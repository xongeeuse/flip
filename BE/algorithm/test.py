import os
import json
import time
import random
import logging
from datetime import datetime, timezone
from typing import List, Tuple, Dict, Any

from confluent_kafka import Consumer, Producer
import redis
from dotenv import load_dotenv

import api  # local module

# ---------------------------------------------------------------------------
# ▶ 환경 설정 & 공용 객체
# ---------------------------------------------------------------------------
load_dotenv()

KAFKA_HOST = os.getenv("KAFKA_HOST", "localhost")
KAFKA_BOOT = os.getenv("KAFKA_BOOT", "localhost:9092")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))

# Kafka
consumer = Consumer({
    "bootstrap.servers": KAFKA_BOOT,
    "group.id": "algorithm-grp",
    # 최신 오프셋부터 읽기 – 자동 커밋 ON
    "auto.offset.reset": "latest",
    "enable.auto.commit": True,
})
producer = Producer({"bootstrap.servers": KAFKA_BOOT})

# Redis (connection‑pool reuse)
r = redis.Redis(host=KAFKA_HOST, port=REDIS_PORT, decode_responses=True)

# 로깅
logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s │ %(levelname)-8s │ %(message)s",
)
log = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# ▶ 유틸리티 함수 (Redis 파이프라인으로 I/O 최소화)
# ---------------------------------------------------------------------------

def hmget_all(keys: List[str]) -> Dict[str, Dict[str, str]]:
    """주어진 해시 키 목록을 HGETALL 파이프라인으로 일괄 조회"""
    pipe = r.pipeline()
    for k in keys:
        pipe.hgetall(k)
    return dict(zip(keys, pipe.execute()))


def mget_strings(keys: List[str]) -> Dict[str, str]:
    """단순 GET 키를 파이프라인으로 일괄 조회"""
    pipe = r.pipeline()
    for k in keys:
        pipe.get(k)
    return dict(zip(keys, pipe.execute()))

# ---------------------------------------------------------------------------
# ▶ 비즈니스 로직 (핵심 hotspot 함수들 최적화)
# ---------------------------------------------------------------------------

def find_start_cancelled_amrs(cancelled_amrs: List[str]) -> List[Tuple[int, int]]:
    """엣지 컷 시, 취소된 AMR들의 (시작노드, 최종노드) 목록을 반환"""
    if not cancelled_amrs:
        return []

    keys = [f"AMR_STATUS:{amr_id}" for amr_id in cancelled_amrs]
    statuses = hmget_all(keys)

    result: List[Tuple[int, int]] = []
    for amr_id, h in zip(cancelled_amrs, statuses.values()):
        if not h:
            continue
        try:
            sub_list_raw = h.get("submissionList", "[]")
            sub_list = [json.loads(s) for s in json.loads(sub_list_raw)]
            nodes = [int(s["submissionNode"]) for s in sub_list if "submissionNode" in s]
            if not nodes:
                continue
            current_idx = int(h.get("submissionId", 0))
            start_node = nodes[min(current_idx, len(nodes) - 1)]
            result.append((start_node, nodes[-1]))
        except Exception:
            continue
    return result


def find_available_charging_zones() -> List[int]:
    """충전소 중 현재 사용 가능한 zone node 번호 반환"""
    used = [False, False, False]  # 91,92,93
    keys = [f"AMR_STATUS:AMR{i:03d}" for i in range(1, 21)]
    statuses = hmget_all(keys)

    for h in statuses.values():
        if not h:
            continue
        if str(h.get("missionType", "")).upper() == "CHARGING":
            try:
                sub_nodes = [int(s["submissionNode"]) for s in json.loads(h["submissionList"])]
                if sub_nodes and 91 <= sub_nodes[-1] <= 93:
                    used[sub_nodes[-1] - 91] = True
            except Exception:
                pass
    return [i + 91 for i, u in enumerate(used) if not u]


def get_low_battery_amrs(threshold: int = 50) -> List[str]:
    """배터리 임계값 이하인 AMR id 목록"""
    keys = [f"AMR_STATUS:AMR{i:03d}" for i in range(1, 21)]
    statuses = hmget_all(keys)
    low = []
    for amr_id, h in zip([f"AMR{i:03d}" for i in range(1, 21)], statuses.values()):
        try:
            if h and int(h.get("battery", 101)) <= threshold:
                low.append(amr_id)
        except (TypeError, ValueError):
            pass
    return low


# ------------------- Robot / Job fetch -------------------

def fetch_robot_list(need_charge: List[str], triggered: str | None, mission_type: str | None, ban_nodes: List[int]) -> Tuple[List[Tuple[str, int, int]], List[int]]:
    """Redis → (amrId, nodeId, loading) 목록 반환 + 작업 금지 node 목록"""
    robot: List[Tuple[str, int, int]] = []
    ban_work = set(ban_nodes)

    keys = [f"AMR_STATUS:AMR{i:03d}" for i in range(1, 21) if f"AMR{i:03d}" not in need_charge]
    statuses = hmget_all(keys)

    for key, h in statuses.items():
        if not h:
            continue
        amr_id = h.get("amrId", key.split(":")[-1])
        loading = 1 if str(h.get("loading", "")).lower() == "true" else 0

        # submission list 파싱
        node_id = int(h.get("currentNode", 0))
        try:
            sub_list = [json.loads(s) for s in json.loads(h.get("submissionList", "[]"))]
            sub_nodes = [int(s["submissionNode"]) for s in sub_list]
            if sub_nodes:
                node_id = sub_nodes[int(h.get("submissionId", 0))]
        except Exception:
            pass

        # 특정 영역 건너뛰기
        if 1 <= node_id <= 10 or 21 <= node_id <= 30 or 41 <= node_id <= 50:
            ban_work.add(node_id if node_id >= 11 else int(h.get("finalGoal", 0)))
            log.debug("%s → %s : 다음 목적지라 제외", amr_id, node_id)
            continue

        if str(h.get("missionType", "")).upper() == "CHARGING" and amr_id == triggered and mission_type == "CHARGING":
            robot.append((amr_id, node_id, 0))
        elif loading == 0:
            robot.append((amr_id, node_id, loading))
        else:
            ban_work.add(int(h.get("finalGoal", 0)))

    return robot, list(ban_work)


def fetch_line_status(banlist: List[int]) -> List[Tuple[int, float]]:
    """MISSION_PT:node 의 최신 timestamp → elapsed 로 점수화"""
    now = datetime.now(timezone.utc).timestamp()
    nodes = [n for n in range(11, 51) if n not in banlist]
    keys = [f"MISSION_PT:{n}" for n in nodes]
    raw = mget_strings(keys)

    result: List[Tuple[int, float]] = []
    for node, val in zip(nodes, raw.values()):
        if not val or val in ("-1", -1):
            continue
        try:
            ts = datetime.fromisoformat(str(val)).timestamp()
            result.append((node, now - ts))
        except Exception:
            log.debug("MISSION_PT:%s 파싱 실패 → %s", node, val)
    return result


# ---------------------------------------------------------------------------
# ▶ Kafka 메시지 처리 루프
# ---------------------------------------------------------------------------

def handle_edge_cut(payload: Dict[str, Any]) -> None:
    cut_edge = int(payload.get("cutEdge", 0))
    cancelled = payload.get("cancelledAmrs", [])
    log.info("[EDGE CUT] edge=%s, cancelled=%s", cut_edge, cancelled)

    api.mapInit(cut_edge)
    starts_ends = find_start_cancelled_amrs(cancelled)
    if not starts_ends:
        return

    assign = api.calEdgeCutRoute(starts_ends, cancelled)
    publish_results(assign)


def publish_results(assign: List[Tuple]):
    results = build_results_from_assign(assign)
    if not results:
        return
    payload = {"missions": results}
    producer.produce("algorithm-result", json.dumps(payload).encode())
    producer.poll(0)  # flush non‑blocking


def listen_loop():
    broken_line: List[int] = []
    broken_node: List[int] = []

    consumer.subscribe(["algorithm-trigger"], on_assign=lambda c, p: log.info("Kafka connected"))

    while True:
        msgs = consumer.consume(32, timeout=1.0)  # batch consume (reduce Python ↔ C overhead)
        for msg in msgs:
            if msg is None or msg.error():
                continue
            raw = msg.value().decode().strip()
            log.debug("📩 %s", raw)

            # ---------------------------------------------
            # 문자열 명령 처리
            # ---------------------------------------------
            if raw.lower() == "simulator start":
                triggered, cancelled_amrs, mission_type = None, [], "START"

            elif raw.startswith("LINE BROKEN"):
                broken_line = [10]
                broken_node = [20, 30]
                # … (이벤트 처리 생략) …
                continue

            elif raw.startswith("LINE REPAIR"):
                broken_line.clear()
                broken_node.clear()
                continue

            elif raw.lower().startswith("none edge error"):
                try:
                    amr_id = raw.split(":")[-1].strip()
                    h = r.hgetall(f"AMR_STATUS:{amr_id}")
                    assign = api.calEdgeCutRoute([(int(h.get("currentNode", 0)), 89)], [amr_id])
                    publish_results(assign)
                except Exception as e:
                    log.error("Edge error handling failed: %s", e)
                continue

            # ---------------------------------------------
            # JSON payload 처리
            # ---------------------------------------------
            elif raw.startswith("{"):
                try:
                    payload = json.loads(raw)
                except json.JSONDecodeError:
                    log.warning("JSON parse error – skip")
                    continue

                if "cutEdge" in payload:
                    handle_edge_cut(payload)
                    continue

                triggered = payload.get("amrId")
                cancelled_amrs = payload.get("cancelledAmrs", [])
                mission_type = payload.get("missionType")
                if not triggered:
                    continue
            else:
                continue  # unknown message

            # ---------------------------------------------
            # (1) 충전 AMR 선별 & (2) 작업 로봇 / 작업지 추출
            # ---------------------------------------------
            zones = find_available_charging_zones()
            need_charge = get_low_battery_amrs()
            robots, ban = fetch_robot_list(need_charge, triggered, mission_type, broken_node)
            jobs = fetch_line_status(ban)
            log.debug("robots=%s, jobs=%s, zones=%s", robots, jobs, zones)

            # ---------------------------------------------
            # 알고리즘 실행
            # ---------------------------------------------
            try:
                assign = api.assign_tasks(robots, jobs)
            except Exception as e:
                log.error("assign_tasks() error: %s", e)
                continue

            # 충전 작업 추가
            charge_starts = []
            for amr in need_charge:
                h = r.hgetall(f"AMR_STATUS:{amr}")
                try:
                    # submission list → 다음 노드 확인
                    node_id = int(h.get("currentNode", 0))
                    sub_nodes = [int(s["submissionNode"]) for s in json.loads(h.get("submissionList", "[]"))]
                    if sub_nodes:
                        node_id = sub_nodes[int(h.get("submissionId", 0))]
                except Exception:
                    pass
                charge_starts.append((node_id, amr))

            assign.extend(api.assign_charging_spots(charge_starts, zones, need_charge))
            publish_results(assign)


# ---------------------------------------------------------------------------
# ▶ 경로 재구성 & 결과 포맷 변환 (기존 로직 그대로)
# ---------------------------------------------------------------------------

def build_results_from_assign(assign):
    """… 중략 – 기존 함수 내용 그대로, 단 producer flush 제거 목적상 유지"""
    all_results = []
    for (amr_id, _, _), (dest, _), mission_type, path, cost in assign:
        if cost >= 900 or path is None:
            continue
        h = r.hgetall(f"AMR_STATUS:{amr_id}")
        submission_nodes: List[int] = []
        try:
            raw_list = [json.loads(s) for s in json.loads(h.get("submissionList", "[]"))]
            submission_nodes = [int(s.get("submissionNode")) for s in raw_list]
            submission_nodes = submission_nodes[: int(h.get("submissionId", 0))]
        except Exception:
            pass

        if submission_nodes and submission_nodes[-1] == path[0]:
            path = submission_nodes[:-1] + path
        else:
            path = submission_nodes + path

        all_results.append({
            "amrId": amr_id,
            "missionId": f"MISSION{int(dest):03d}",
            "missionType": mission_type,
            "route": path,
            "expectedArrival": int(cost) // 2,
        })
    return all_results

# ---------------------------------------------------------------------------
# ▶ main
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    api.mapInit()  # 초기 edge map 로딩
    try:
        listen_loop()
    finally:
        consumer.close()
        producer.flush()
