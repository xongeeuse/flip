import simpy.rt
import threading
import time
import math
import websocket
import json
import random
from datetime import datetime
import os
# 최상단
from dotenv import load_dotenv

load_dotenv()  # .env 로부터 환경 변수 로드


# ---------- 설정 ----------
REALTIME_INTERVAL = 0.01  # 좌표 갱신 주기 (초)
SHARED_STATUS = {}        # 모든 AMR의 실시간 위치 상태
LOCK = threading.RLock()   # 공유 자원 보호
map_data = None
amrs = []  # <- 전역 AMR 리스트
INTERSECTING_EDGE_PAIRS = set()
NODE_RESERVATIONS = {}
simulation_started = False
REQUEST_DIST = 1.9
MAX_WAIT_BEFORE_IGNORE = 10.0
AMR_WS_URL = os.getenv("AMR_WS_URL","ws://localhost:8080/ws/amr")
PERSON_WS_URL = os.getenv("PERSON_WS_URL","ws://localhost:8080/ws/human")
person=None
person_ws = None

if not AMR_WS_URL:
    raise RuntimeError("환경 변수 AMR_WS_URL 이 설정되지 않았습니다.")

# ---------- 메시지 핸들러 ----------
def on_open(ws):
    print(f"✅ WebSocket 연결 완료")

def on_close(ws):
    print(f"❌ WebSocket 연결 해제")

def on_message(ws, message):

    try:
        data = json.loads(message)
        msg_name = data.get("header", {}).get("msgName")
        # print(f"▶ PARSED msgName={repr(msg_name)}")  # ← 추가

        if   msg_name == "MAP_INFO":
            handle_map_info(data, ws)
        elif msg_name == "MISSION_ASSIGN":
            handle_mission_assign(data)
        elif msg_name == "MISSION_CANCEL":
            handle_mission_cancel(data)
        elif msg_name == "TRAFFIC_PERMIT":
            handle_traffic_permit(data)
        else:
            print("▶ Unhandled msgName:", repr(msg_name))  # ← 추가
    except Exception as e:
        print(f"❌ 메시지 처리 오류: {e}")


# ---------- WebSocket 서버 ----------
def make_ws_client():
    return websocket.WebSocketApp(
        AMR_WS_URL,
        on_message=on_message,
        on_open=on_open,
        on_close=on_close
    )

ws_clients = [make_ws_client() for _ in range(20)]

# ---------- 메시지 처리 함수 ----------
def handle_map_info(data, ws):
    global map_data, simulation_started
    if not simulation_started:
        simulation_started = True
        # print("[MAP_INFO] 맵 데이터 수신 완료", data)
        raw_map = data['body']['mapData']

        nodes = {}
        for node in raw_map['areas']['nodes']:
            nodes[str(node['nodeId'])] = {
                'id': node['nodeId'],
                'x': node['worldX'],
                'y': node['worldY'],
                'nodeName' : node['nodeName'],
                'nodeType' : node['nodeType'],
                'direction': node['direction']
            }

        edges = {}
        for edge in raw_map['areas']['edges']:
            edges[str(edge['edgeId'])] = {
                'node1': edge['node1'],
                'node2': edge['node2'],
                'speed': edge['speed'],
                'edgeDirection': edge['edgeDirection']
            }

        map_data = {
            "nodes": nodes,
            "edges": edges
        }

        INTERSECTING_EDGE_PAIRS.update(compute_intersecting_edges(map_data))


        print("✅ 맵 저장 완료! 시뮬레이션 시작")
        start_simulation()
    # else:
    #     print("⚠️ 시뮬레이션 이미 시작됨, 재시작 생략")


import threading
import json
from datetime import datetime

def handle_mission_assign(data):
    if ("CHARGING" == data['body']['missionType']):
        print("[MISSION_ASSIGN] 미션 수신:", data)
    mission = data['body']
    target_amr_id = data['header']['amrId']

    for amr in amrs:
        if amr.id != target_amr_id:
            continue

        # 새 미션 수신 즉시 기존 타이머 취소
        if hasattr(amr, 'mission_request_timer') and amr.mission_request_timer:
            amr.mission_request_timer.cancel()
            amr.mission_request_timer = None

        # 1. 동시성 방지용 락 걸고, 마지막 완료 sub ID 읽기
        with LOCK:
            last_done = amr.current_submission_id

        # 2. 이미 완료한 서브미션(<= last_done) 은 건너뛰고
        raw_subs = mission["submissions"]
        if last_done is not None:
            filtered_subs = [
                sub for sub in raw_subs
                if sub["submissionId"] > last_done
            ]
        else:
            filtered_subs = raw_subs

        if not filtered_subs:
            return

        # 3. 기존 미션 즉시 중단, 새로운 서브미션만 큐에 넣기
        amr.current_mission_id   = mission["missionId"]
        amr.current_mission_type = mission["missionType"]
        amr.assign_mission({
            "missionId":   mission["missionId"],
            "missionType": mission["missionType"],
            "submissions": filtered_subs
        })
        # if ("AMR008" == data['header']['amrId']):
        #     print(f"✅ {amr.id} 새 미션 수락 완료 (sub {filtered_subs[0]['submissionId']}부터 수행)")
        return


def handle_mission_cancel(data):
    # if ("AMR008" == data['header']['amrId']):
    #     print("[MISSION_CANCEL] 미션 취소 수신:", data)
    target_amr_id = data['header']['amrId']

    for amr in amrs:
        if amr.id == target_amr_id:
            # ① 취소 플래그 & 즉시 정지
            amr.interrupt_flag = True
            amr.current_speed = 0

            # ② 기존 타이머가 있으면 취소
            if hasattr(amr, 'mission_request_timer') and amr.mission_request_timer:
                amr.mission_request_timer.cancel()

            # ③ 5초 후에도 새 미션이 없으면 요청
            def request_mission():
                if amr.current_mission_data is None:
                    req = {
                        "header": {
                            "msgName": "MISSION_REQ",
                            "time": datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
                        },
                        "body": {
                            "amrId": amr.id
                        }
                    }
                    idx = int(amr.id[-3:]) - 1
                    try:
                        ws_clients[idx].send(json.dumps(req))
                        print(f"▶ {amr.id} 미션 요청 전송")
                    except Exception as e:
                        print(f"❌ {amr.id} 미션 요청 실패: {e}")

            amr.mission_request_timer = threading.Timer(5.0, request_mission)
            amr.mission_request_timer.daemon = True
            amr.mission_request_timer.start()

            return


# ---------- 메시지 처리 함수 ----------
def handle_traffic_permit(data):

    header = data.get("header", {})
    amr_id = header.get("amrId")
    if not amr_id:
        print(f"❌ amrId 없음: {data}")
        return

    permit = data.get("body", {})
    node_id = permit.get("nodeId")

    # print(f"[DEBUG] {amr_id} got TRAFFIC_PERMIT for node {node_id} at env.now={env.now if 'env' in globals() else 'unknown'}")


    # amrId만으로 바로 처리
    for amr in amrs:
        if amr.id != amr_id:
            continue

        with LOCK:
            NODE_RESERVATIONS[node_id] = amr.id
            # print(f"✅ {amr.id} 노드 {node_id} 접근 예약됨")

        amr.traffic_event.set()
        break



# ---------- 교차 간선 계산 ----------
def edges_intersect(p1, p2, q1, q2):
    def ccw(a, b, c):
        return (c["y"] - a["y"]) * (b["x"] - a["x"]) > (b["y"] - a["y"]) * (c["x"] - a["x"])
    return ccw(p1, q1, q2) != ccw(p2, q1, q2) and ccw(p1, p2, q1) != ccw(p1, p2, q2)

def compute_intersecting_edges(map_data):
    intersecting_edge_pairs = set()
    edges = map_data["edges"]
    nodes = map_data["nodes"]
    edge_list = []

    for eid, edge in edges.items():
        n1 = nodes[str(edge["node1"])]
        n2 = nodes[str(edge["node2"])]
        edge_list.append((eid, {"x": n1["x"], "y": n1["y"]}, {"x": n2["x"], "y": n2["y"]}))

    for i in range(len(edge_list)):
        eid1, a1, a2 = edge_list[i]
        for j in range(i + 1, len(edge_list)):
            eid2, b1, b2 = edge_list[j]
            if edges_intersect(a1, a2, b1, b2):
                intersecting_edge_pairs.add((eid1, eid2))
                intersecting_edge_pairs.add((eid2, eid1))

    return intersecting_edge_pairs



def start_simulation():
    global amrs  # ✅ 전역 변수 사용 선언!
    global person

    if map_data is None:
        print("❌ 맵 데이터 없음. 시뮬레이션 시작할 수 없음.")
        return

    print("🚀 시뮬레이션 시작!")

    # 마지막 WebSocket 클라이언트에만 시작 메시지 전송
    last_ws = ws_clients[-1]
    start_message = {
        "header": {
            "msgName": "SIMULATION_START",
            "time": datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
        },
        "body": {
            "message": "Simulation has started"
        }
    }
    if last_ws.sock and last_ws.sock.connected:
        try:
            last_ws.send(json.dumps(start_message))
        except Exception as e:
            print(f"[WARN] 시작 메시지 전송 실패: {e}")

    amrs = setup_amrs(env, map_data)  # ✅ 전역 amrs에 저장
    person = setup_person(env)

    threading.Thread(target=broadcast_status, daemon=True).start()
    threading.Thread(target=lambda: env.run(), daemon=True).start()
    threading.Thread(target=broadcast_person_status, daemon=True).start()


# ---------- AMR 클래스 ----------
class AMR:
    def __init__(self, env, amr_id, map_data, pos_x, pos_y, type, current_node_id):
        self.env = env
        self.id = amr_id
        self.map_data = map_data
        self.pos_x = pos_x
        self.pos_y = pos_y
        self.dir = 0  # 방향(degree)
        self.state = 1  # 1: IDLE, 2: PROCESSING
        self.battery = random.randint(60, 100)

        self.current_mission_data = None
        self.current_mission = None
        self.interrupt_flag = False

        self.current_mission_id = "DUMMY"
        self.current_mission_type = None
        self.current_submission_id = None
        self.current_speed = 0
        self.loaded = False
        self.current_node_id = current_node_id
        self.type = type
        self.waiting_for_traffic = None  # (missionId, submissionId, nodeId)
        self.traffic_event = threading.Event()
        self.current_edge_id = None
        self.is_avoiding = False
        self.permit_requested = False

        self.mission_request_timer = None

    def update_status(self):
        with LOCK:
            SHARED_STATUS[self.id] = {
                "id": self.id,
                "x": self.pos_x,
                "y": self.pos_y,
                "dir": self.dir,
                "state": self.state,
                "battery": self.battery,
                "missionId": self.current_mission_id,
                "missionType": self.current_mission_type,
                "submissionId": self.current_submission_id,
                "currentNode": self.current_node_id,
                "speed": self.current_speed,
                "loaded": self.loaded,
                "timestamp": time.time(),
                "type": self.type,
                "currentEdge": self.current_edge_id,
                "isAvoiding": self.is_avoiding,

            }

    def assign_mission(self, mission):

        self.interrupt_flag = True
        self.current_mission_data = mission

    def run(self):
        while True:
            if self.interrupt_flag:
                self.interrupt_flag = False
                self.current_mission = None

            if self.current_mission_data:
                self.current_mission = self.current_mission_data
                self.current_mission_data = None
                yield from self.process_mission(self.current_mission)
                self.current_mission = None
            else:
                yield self.env.timeout(REALTIME_INTERVAL)

    def process_mission(self, mission):
        self.state = 2
        self.current_mission_id = mission["missionId"]
        self.current_mission_type = mission["missionType"]
        self.update_status()
        prev = self.map_data["nodes"][str(self.current_node_id)]

        aborted = False
        for sub in mission["submissions"]:
            if self.interrupt_flag:
                # 중간 취소 감지
                aborted = True
                self.interrupt_flag = False
                # print(f"🚫 {self.id} 미션 중단 (sub {self.current_submission_id})")
                break

            # 다음 서브미션 실행
            self.current_submission_id = sub["submissionId"]
            self.current_edge_id = sub["edgeId"]
            node = self.map_data["nodes"][str(sub["nodeId"])]
            edge = self.map_data["edges"][str(sub["edgeId"])]
            self.current_speed = edge["speed"]

            yield from self.move_to_node(node, edge, prev)
            prev = node
        self.current_node_id = prev["id"]
        self.update_status()

        # ─── 완료/중단 후 초기화 ─────────────────────────────────────────

        # **취소(aborted)가 아닌 정상 완료일 때만 loaded 토글**
        if aborted:
            self.current_speed=0
            self.update_status()
            return

        self.state = 1
        if self.current_mission_type == "LOADING":
            self.loaded = True
        elif self.current_mission_type == "UNLOADING":
            self.loaded = False
        self.current_submission_id = None

        # 공통 초기화
        # self.current_mission_id = None
        # self.current_mission_type = None
        # self.current_edge_id = None
        self.current_speed = 0
        self.update_status()

    def move_to_node(self, node, edge, prev):
        # if(self.id == "AMR008"):
        #     print("목표 노드: ", node["id"])
        # ─── 상수 정의 ─────────────────────────────────────
        STOP_DIST = 1.2
        RESUME_DIST = 1.7
        MIN_PAUSE_TIME = 0.3
        REQUEST_DIST = 1.9
        MAX_WAIT_BEFORE_IGNORE = 10.0

        # ─── 직선 이동 계산 ─────────────────────────────────
        distance = self.get_distance(self.pos_x, self.pos_y, node["x"], node["y"])
        speed = edge["speed"]
        duration = distance / speed
        steps = max(1, int(duration / REALTIME_INTERVAL))
        dx = (node["x"] - self.pos_x) / steps
        dy = (node["y"] - self.pos_y) / steps
        per_step_dist = speed * REALTIME_INTERVAL  # 한 스텝당 전진 거리

        # ─── 회전 정렬 ─────────────────────────────────────
        angle_rad = math.atan2(dy, dx)
        angle_std = math.degrees(angle_rad) % 360
        target_dir = (90 - angle_std) % 360
        diff = (target_dir - self.dir + 360) % 360
        if diff > 180: diff -= 360
        turn_speed = 360 / 3
        turn_per_step = turn_speed * REALTIME_INTERVAL
        steps_to_turn = int(abs(diff) / turn_per_step)

        for _ in range(steps_to_turn):
            yield self.env.timeout(REALTIME_INTERVAL)
            self.dir = (self.dir + turn_per_step * (1 if diff > 0 else -1)) % 360
            self.update_status()
        self.dir = target_dir
        self.current_node_id = prev["id"]
        self.update_status()

        # ─── TRAFFIC_REQ 준비 ───────────────────────────────
        self.traffic_event.clear()
        self.waiting_for_traffic = (
            self.current_mission_id,
            self.current_submission_id,
            node["id"]
        )
        self.permit_requested = False
        self.collision_ignored = False

        # ─── 회피 및 복귀 변수 초기화 ────────────────────────
        avoidance_steps = 0
        avoidance_steps_orig = 0
        avoidance_dx = avoidance_dy = 0.0
        return_steps = 0
        return_dx = return_dy = 0.0

        # ─── 회피 후 원래 방향 유지 스텝 수 & 플래그 ───────────────────
        PRE_RETURN_STEPS = 50
        saved_offset_x = 0.0
        saved_offset_y = 0.0
        pre_returned = False

        # ─── 이동 + 회피 루프 ────────────────────────────────
        for step_idx in range(steps):
            # 1) 시간 경과 & 전진
            yield self.env.timeout(REALTIME_INTERVAL)
            # ── 사람과 충돌 우회 대기 ─────────────────────────
            ps = SHARED_STATUS.get(person.id)
            if ps:
                px = ps['x'] - self.pos_x
                py = ps['y'] - self.pos_y
                dist_p = math.hypot(px, py)

                # 전방 60°(±30°) 영역만 탐지
                ANG_THRESH = 30  # 반각(°)
                # 이동 벡터와 사람 벡터의 내적
                dot = dx * px + dy * py
                # 벡터 크기 곱
                denom = math.hypot(dx, dy) * dist_p
                # 코사인 임계값
                cos_thresh = math.cos(math.radians(ANG_THRESH))

                # 거리 2m 이내 & 두 벡터 각도 ≤ ±30°
                if dist_p <= 2.0 and denom > 0 and dot / denom >= cos_thresh:
                    # 사람이 완전히 벗어날 때까지 대기
                    while True:
                        yield self.env.timeout(REALTIME_INTERVAL)
                        s2 = SHARED_STATUS.get(person.id)
                        if not s2:
                            break
                        px2 = s2['x'] - self.pos_x
                        py2 = s2['y'] - self.pos_y
                        if math.hypot(px2, py2) > 2.0:
                            yield self.env.timeout(0.5)
                            break
                    continue

            self.pos_x += dx
            self.pos_y += dy

            # 3) 사전 복귀 트리거 (도착 PRE_RETURN_STEPS 전) — 한 번만
            if (not pre_returned
                    and (saved_offset_x != 0.0 or saved_offset_y != 0.0)
                    and avoidance_steps == 0 and return_steps == 0
                    and step_idx == steps - PRE_RETURN_STEPS):
                pre_returned = True
                return_steps = PRE_RETURN_STEPS
                yield self.env.timeout(REALTIME_INTERVAL)
                return_dx = -saved_offset_x / PRE_RETURN_STEPS
                return_dy = -saved_offset_y / PRE_RETURN_STEPS
                # 즉시 복귀 방향으로 회전
                ang = math.atan2(return_dy, return_dx)
                self.dir = (90 - math.degrees(ang) % 360) % 360
                self.update_status()
                # 재발동 방지
                saved_offset_x = saved_offset_y = 0.0

            # 2) 측면 회피 중이면
            if avoidance_steps > 0:
                # # ─── 디버깅 로그 ───────────────────────────────────
                # print(
                #     f"[DEBUG] step={step_idx:4d} | "
                #     f"avoidance_steps={avoidance_steps:2d} | "
                #     f"return_steps={return_steps:2d} | "
                #     f"pos=({self.pos_x:6.2f},{self.pos_y:6.2f})"
                # )
                self.pos_x += avoidance_dx
                self.pos_y += avoidance_dy
                self.update_status()
                yield self.env.timeout(REALTIME_INTERVAL/2)
                avoidance_steps -= 1
                # 회피 끝나면 복귀 설정
                # if avoidance_steps == 0:
                #     # 복귀 세팅
                #     return_steps = avoidance_steps_orig
                #     return_dx, return_dy = -avoidance_dx, -avoidance_dy
                continue

            # 5) 복귀 중이면
            if return_steps > 0:
                self.pos_x += return_dx
                self.pos_y += return_dy
                self.update_status()
                yield self.env.timeout(REALTIME_INTERVAL)
                return_steps -= 1
                continue

                # if return_steps == 0:
                #     print("[DEBUG] → 복귀 완료")

                # 4) 배터리 소모 · 상태 갱신
            self.battery = max(0, self.battery - 0.0004)
            self.update_status()

            # 5) TRAFFIC_REQ 한 번만
            node_dist = self.get_distance(self.pos_x, self.pos_y, node["x"], node["y"])
            if not self.permit_requested and node_dist <= REQUEST_DIST:
                req = {
                    "header": {
                        "msgName": "TRAFFIC_REQ",
                        "time": datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
                    },
                    "body": {
                        "missionId": self.current_mission_id,
                        "submissionId": self.current_submission_id,
                        "nodeId": node["id"],
                        "amrId": self.id
                    }
                }
                ws_clients[int(self.id[-3:]) - 1].send(json.dumps(req))
                self.permit_requested = True

            # 6) 충돌 감지
            if avoidance_steps == 0 and return_steps == 0 and not self.collision_ignored:
                waiting_node = self.waiting_for_traffic[2]
                for other_id, other in SHARED_STATUS.items():
                    if other_id == self.id:
                        continue
                    if waiting_node and NODE_RESERVATIONS.get(waiting_node) == self.id:
                        break
                    if other["state"] != 2:
                        continue

                    # 헤드온 각도 체크
                    other_dir = other["dir"]
                    heading_diff = abs((other_dir - self.dir + 180) % 360 - 180)
                    vx, vy = other["x"] - self.pos_x, other["y"] - self.pos_y
                    dist = math.hypot(vx, vy)
                    # 충돌 조건
                    if dist <= STOP_DIST:
                        # 헤드온일 때만 측면 회피
                        if heading_diff > 120:
                            # 측면 단위 벡터
                            latx, laty = -dy, dx
                            norm = math.hypot(latx, laty) or 1
                            latx, laty = latx / norm, laty / norm

                            # 10스텝 동안 부드럽게 회피
                            avoidance_steps = 60
                            avoidance_dx = latx * per_step_dist
                            avoidance_dy = laty * per_step_dist
                            avoidance_steps_orig = avoidance_steps  # 복귀용 저장
                            saved_offset_x = avoidance_dx * avoidance_steps_orig
                            saved_offset_y = avoidance_dy * avoidance_steps_orig
                            break

                        # 그 외 기존 멈춤+백오프
                        dot = dx * vx + dy * vy
                        if dot <= 0:
                            continue
                        cos_a = max(-1.0, min(1.0, dot / (math.hypot(dx, dy) * dist)))
                        angle = math.degrees(math.acos(cos_a))
                        if angle > 60:
                            continue

                        # print(f"⛔ {self.id} stopping: {other_id} at {dist:.2f}m, angle {angle:.1f}°")
                        backoff = random.uniform(0, 0.2)
                        # print(f"⏳ {self.id} backing off {backoff:.2f}s")
                        yield self.env.timeout(backoff)

                        pause_start = self.env.now
                        resume_dead = self.env.now + MIN_PAUSE_TIME
                        while True:
                            yield self.env.timeout(REALTIME_INTERVAL)
                            if self.env.now < resume_dead:
                                continue
                            if self.env.now - pause_start >= MAX_WAIT_BEFORE_IGNORE:
                                # print(
                                #     f"⚠️ {self.id} waited {MAX_WAIT_BEFORE_IGNORE}s, ignoring collision with {other_id}")
                                self.collision_ignored = True
                                break
                            s = SHARED_STATUS.get(other_id)
                            if not s:
                                break
                            if math.hypot(s["x"] - self.pos_x, s["y"] - self.pos_y) >= RESUME_DIST:
                                # print(
                                #     f"✅ {self.id} resuming: {other_id} now {math.hypot(s['x'] - self.pos_x, s['y'] - self.pos_y):.2f}m away")
                                break
                        break

            # 7) TRAFFIC_PERMIT 대기
            node_dist = self.get_distance(self.pos_x, self.pos_y, node["x"], node["y"])
            if node_dist <= STOP_DIST and not self.traffic_event.is_set():
                while not self.traffic_event.is_set():
                    yield self.env.timeout(REALTIME_INTERVAL)

        # 4. 위치 정렬
        self.pos_x = node["x"]
        self.pos_y = node["y"]
        # self.current_node_id = node["id"]
        self.update_status()

        # 5. 노드 방향 회전 처리 (charging, docking 등)
        if node["nodeType"] == "DOCKING":
            target_dir = node["direction"]
            diff = (target_dir - self.dir + 360) % 360
            if diff > 180:
                diff -= 360

            turn_speed = 360 / 3  # 120 deg/sec
            turn_per_step = turn_speed * REALTIME_INTERVAL
            steps_to_turn = int(abs(diff) / turn_per_step)

            for _ in range(steps_to_turn):
                yield self.env.timeout(REALTIME_INTERVAL)
                self.dir = (self.dir + turn_per_step * (1 if diff > 0 else -1)) % 360
                self.update_status()

            self.dir = target_dir
            self.update_status()
        if node["nodeType"] == "CHARGE" and self.current_mission_type == "CHARGING":
            target_dir = node["direction"]
            diff = (target_dir - self.dir + 360) % 360
            if diff > 180:
                diff -= 360

            turn_speed = 360 / 3  # 120 deg/sec
            turn_per_step = turn_speed * REALTIME_INTERVAL
            steps_to_turn = int(abs(diff) / turn_per_step)

            for _ in range(steps_to_turn):
                yield self.env.timeout(REALTIME_INTERVAL)
                self.dir = (self.dir + turn_per_step * (1 if diff > 0 else -1)) % 360
                self.update_status()

            self.dir = target_dir
            self.update_status()

        # 6. docking 노드는 도착 후 작업 시간 5초간 대기
        if node["nodeType"] == "DOCKING":
            # print(f"🛠️ {self.id} docking 작업 중 (5초)")
            for _ in range(int(5 / REALTIME_INTERVAL)):
                yield self.env.timeout(REALTIME_INTERVAL)

        # 7. charging 노드에서는 충전
        if node["nodeType"] == "CHARGE" and self.current_mission_type == "CHARGING":
            print(f"🔋 {self.id} 충전 시작 (100초 동안 1%씩)")
            self.state=3
            self.update_status()
            for _ in range(int(300 / REALTIME_INTERVAL)):  # 100초 = 1초당 1%
                self.battery += 0.007
                if self.battery > 100:
                    self.battery = 100
                    break
                self.update_status()
                yield self.env.timeout(REALTIME_INTERVAL)
            self.state=2
            self.update_status()


        with LOCK:
            if NODE_RESERVATIONS.get(self.current_node_id) == self.id:
                del NODE_RESERVATIONS[self.current_node_id]


    def get_distance(self, x1, y1, x2, y2):
        return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)






# ---------- AMR 초기화 ----------
def setup_amrs(env, map_data):
    amrs = []

    amr_start_positions = [
        (119, 21.5, 6.5),
        (175, 31.5, 73.5),
        (131, 73.5, 6.5),
        (202, 10.5, 19.5),
        (160, 44.5, 41.5),
        (117, 13.5, 6.5),
        (223, 73.5, 54.5),
        (189, 21.5, 76.5),
        (142, 44.5, 38.5),
        (156, 26.5, 41.5),
        (121, 31.5, 6.5),
        (137, 21.5, 38.5),
        (230, 70.5, 60.5),
        (198, 62.5, 76.5),
        (166, 70.5, 41.5),
        (115, 7.5, 6.5),
        (153, 13.5, 41.5),
        (187, 13.5, 76.5),
        (124, 44.5, 6.5),
        (216, 76.5, 25.5),
    ]

    for i, (n, x, y) in enumerate(amr_start_positions):
        amr_id = f"AMR{str(i + 1).zfill(3)}"
        amr_type = 0 if i < 10 else 1  # 0번~9번 → type=0, 10번~19번 → type=1
        amr = AMR(env, amr_id, map_data, x, y, amr_type, n)
        amr.battery=50+i*2
        amr.update_status()
        env.process(amr.run())
        amrs.append(amr)

    return amrs



def broadcast_status():
    while True:
        try:
            with LOCK:
                if not SHARED_STATUS:
                    time.sleep(0.1)
                    continue

                now = datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
                # SHARED_STATUS.items()를 리스트로 복사해두면 루프 도중 변경에도 안전합니다
                for i, (entity_id, status) in enumerate(list(SHARED_STATUS.items())):
                    # "battery"가 없는 엔트리(예: Person)는 건너뛴다
                    if "battery" not in status:
                        continue

                    message = {
                        "header": {
                            "msgName": "AMR_STATE",
                            "time": now
                        },
                        "body": {
                            "worldX": status["x"],
                            "worldY": status["y"],
                            "dir": status["dir"],
                            "amrId": status["id"],
                            "state": status["state"],
                            "battery": status["battery"],
                            "currentNode": status.get("currentNode", ""),
                            "currentEdge": status.get("currentEdge", ""),
                            "loading": bool(status["loaded"]),
                            "missionId": status.get("missionId", ""),
                            "submissionId": status.get("submissionId", "0"),
                            "linearVelocity": status.get("speed", 0),
                            "missionType": status.get("missionType", 0),
                            "errorList": ""
                        }
                    }

                    # AMR용 WebSocket 클라이언트에만 전송
                    if i < len(ws_clients):
                        try:
                            ws_clients[i].send(json.dumps(message))
                        except Exception as e:
                            print(f"❌ [BROADCAST] 전송 실패: {e}")
                            ws_clients[i].close()

            time.sleep(0.1)

        except Exception as global_exception:
            print(f"❌ [BROADCAST] 스레드 종료: {global_exception}")



# ─── Person 클래스 수정 ────────────────────────────────
class Person:
    def __init__(self, env, person_id, start_x, start_y, pause_points=None, pause_time=3.0, move_speed=0.35):
        self.env = env
        self.id = person_id
        # 평소에는 숨김 위치
        self.pos_x = 1000.0
        self.pos_y = 1000.0
        self.dir = 0                # 방향
        self.route = None
        self.route_index = 0
        self.go_back = False
        self.state = 0              # 0: IDLE, 1: MOVING, 2: WAITING, 3: ROT_CCW, 4: ROT_CW

        # 멈출 좌표 리스트 ([(x1,y1), (x2,y2), ...])
        self.pause_points = pause_points or []
        # 멈춤 시간(초)
        self.pause_time   = pause_time
        self.move_speed = move_speed

    def update_status(self):
        with LOCK:
            SHARED_STATUS[self.id] = {
                "id":    self.id,
                "x":     self.pos_x,
                "y":     self.pos_y,
                "dir":   self.dir,
                "state": self.state,
            }

    def walk_to(self, tx, ty):
        """
        이동 전 회전: state=3(왼쪽 CCW) or 4(오른쪽 CW), 1초간 대기
        그 뒤 state=1 이동, 도착 후 state=0 IDLE
        방향 기준을 AMR과 동일하게: 0°=up, 90°=right, 180°=down, 270°=left
        """
        # 1) 목표 벡터
        dx = tx - self.pos_x
        dy = ty - self.pos_y

        # 2) 표준 atan2() 각도를 0°=right 기준으로 구한 뒤,
        #    AMR 기준인 0°=up 으로 변환
        #    angle_std: degrees from +x-axis (0°=right, CCW positive)
        angle_std   = (math.degrees(math.atan2(dy, dx)) + 360) % 360
        # desired_dir: 0°=up, CW positive
        desired_dir = (90 - angle_std) % 360

        # 3) 회전량 diff 계산 (–180, +180] 범위)
        diff = (desired_dir - self.dir + 540) % 360 - 180

        # 4) 회전할 필요가 있으면 state 설정 후 1초 대기
        if abs(diff) > 1e-3:
            # AMR 코드와 동일하게, diff>0 이면 CW(시계) 회전
            if diff > 0:
                self.state = 3   # ROT_CW
            else:
                self.state = 4   # ROT_CCW
            self.update_status()

            # 1초간 회전 시뮬레이션
            wait_steps = int(1.0 / REALTIME_INTERVAL)
            for _ in range(wait_steps):
                yield self.env.timeout(REALTIME_INTERVAL)

            # 회전 끝내고 정확한 각도로 설정
            self.dir   = desired_dir
            self.state = 1   # 이동 상태으로 전환
            self.update_status()
        else:
            # 방향 거의 일치하면 바로 이동 상태
            self.dir   = desired_dir
            self.state = 1
            self.update_status()

        # --- 나머지 이동 로직은 기존과 동일 ---
        dist = math.hypot(dx, dy)
        if dist < 1e-3:
            self.state = 0
            self.update_status()
            return

        steps   = max(1, int(dist / (self.move_speed * REALTIME_INTERVAL)))
        step_dx = dx / steps
        step_dy = dy / steps

        for _ in range(steps):
            yield self.env.timeout(REALTIME_INTERVAL)
            self.pos_x += step_dx
            self.pos_y += step_dy
            self.update_status()

        self.state = 0
        self.update_status()

    def run(self):
        while True:
            if self.route:
                # 시작 시 첫 좌표로 순간이동
                if self.route_index == 0 and not self.go_back:
                    sx, sy = self.route[0]
                    self.pos_x, self.pos_y = sx, sy
                    self.state = 0
                    self.update_status()

                target = self.route[self.route_index]
                # 1) 목표 지점까지 이동
                yield from self.walk_to(*target)

                if target in self.pause_points:
                    self.state = 0  # WAITING
                    self.update_status()
                    pause_steps = int(self.pause_time / REALTIME_INTERVAL)
                    for _ in range(pause_steps):
                        yield self.env.timeout(REALTIME_INTERVAL)
                    # 멈춤 끝나면 다시 이동 상태로
                    self.state = 1
                    self.update_status()

                # 2) 순방향/되돌아오기 처리
                if not self.go_back:
                    if self.route_index == len(self.route) - 1:
                        # 끝점 도착 → 5초 대기
                        self.state = 2
                        self.update_status()
                        wait_steps = int(15.0 / REALTIME_INTERVAL)
                        for _ in range(wait_steps):
                            yield self.env.timeout(REALTIME_INTERVAL)
                        self.go_back = True
                        self.route_index -= 1
                    else:
                        self.route_index += 1
                else:
                    if self.route_index == 0:
                        # 되돌아오기 완료 → 숨김 위치로 순간이동
                        self.route      = None
                        self.go_back    = False
                        self.state      = 0
                        self.pos_x, self.pos_y = 1000.0, 1000.0
                        self.update_status()
                    else:
                        self.route_index -= 1
            else:
                # 경로 없으면 숨김 위치 유지
                yield self.env.timeout(REALTIME_INTERVAL)


# ─── WebSocket 메시지 핸들러에 경로 트리거 추가 ─────────────────
def on_person_open(ws):
    print("🙋 Person 연결됨")

def on_person_message(ws, message):
    global person
    try:
        data = json.loads(message)
        if data.get("header", {}).get("msgName") == "MOVE_TRIGGER":
            # 이미 이동 중이면 중첩 방지
            if person.route is not None:
                print("🙋 이미 이동 중이므로 새로운 명령을 무시합니다.")
                return

            # 이동 경로가 비어 있을 때만 새로 설정
            person.route = [
                (80.0, 6.0),
                (79.0, 6.0),
                (79.0, 16.0),
                (69.3, 16.0),
                (69.3, 22.0),
                (69.2, 22.0),
            ]
            person.route_index = 0
            person.go_back    = False
            print("🙋 Person: 이동 경로 설정 완료!")
    except Exception as e:
        print("❌ Person 메시지 처리 오류:", e)



# ─── PERSON_WS 설정에서 on_message에 연결 ─────────────────
def setup_person(env):
    p = Person(env, "PERSON001", start_x=1000.0, start_y=1000.0)
    p.update_status()
    env.process(p.run())
    return p

# ─── 브로드캐스트 루프 ────────────────────────────────
def broadcast_person_status():
    global person_ws
    while True:
        with LOCK:
            status = SHARED_STATUS.get(person.id)
        if status:
            now = datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
            message = {
                "header": {"msgName": "HUMAN_STATE", "time": now},
                "body": {
                    "worldX":  status["x"],
                    "worldY":  status["y"],
                    "dir":     status["dir"],
                    "humanId": status["id"],
                    "state":   status["state"],
                }
            }
            try:
                person_ws.send(json.dumps(message))
            except Exception:
                pass
        time.sleep(0.1)




# ---------- 메인 ----------
if __name__ == '__main__':
    env = simpy.rt.RealtimeEnvironment(factor=0.35, strict=False)
    for ws in ws_clients:
        threading.Thread(target=ws.run_forever, daemon=True).start()

    person_ws = websocket.WebSocketApp(
        PERSON_WS_URL,
        on_open=on_person_open,
        on_message=on_person_message,
        on_close=lambda ws, code, msg: print("🙋 Person 연결 해제")
    )
    threading.Thread(target=person_ws.run_forever, daemon=True).start()


    # 🔒 메인 스레드가 종료되지 않도록 유지
    while True:
        time.sleep(1)


