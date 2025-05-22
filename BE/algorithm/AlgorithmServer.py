from confluent_kafka import Consumer, Producer
import json, redis, api, time, os
import redis, json, time
from datetime import datetime, timezone, timedelta
import random
import api   # 같은 디렉터리의 api.py 임포트
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()
KAFKA_HOST = os.getenv('KAFKA_HOST',"localhost")
KAFKA_BOOT = os.getenv("KAFKA_BOOT", "localhost:9092")


consumer = Consumer({
    "bootstrap.servers": KAFKA_BOOT,
    "group.id": "algorithm-grp",
    "auto.offset.reset": "latest",         # 최신 메시지만
    "enable.auto.commit": True             # ✅ 자동 커밋
})

producer = Producer({"bootstrap.servers": KAFKA_BOOT})

r = redis.Redis(host=KAFKA_HOST, port=6379, decode_responses=True)

def findStartCancelledAmrs(cancelled_amrs):
    startCancelStartEndNode=[]
    for amr_id in cancelled_amrs:
        key = f"AMR_STATUS:{amr_id}"
        h = r.hgetall(key)
        if "submissionList" in h:
            try:
                submission_list = [json.loads(s) for s in json.loads(h["submissionList"])]
                # submissionNode 목록만 추출
                submission_nodes = [s.get("submissionNode") for s in submission_list]
                # currentNode가 submissionList에 있다면, 그 다음 submissionNode 사용
                if len(submission_nodes)!=0:
                    node_id = submission_nodes[int(h.get("submissionId", 0))]
                else:
                    print("심각한 오류 : 엣지를 끊었는데 submissionNode를 찾을수 없는 경우")
                    pass  # 그대로 current_node 유지
                startCancelStartEndNode.append((node_id,submission_nodes[-1]))

            except Exception as e:
                pass
    return startCancelStartEndNode


def findAvailableChargingZones():
    """
    몇 번 충전구역이 사용 가능한지 구하는 함수
    예시 : [91,93] 현재 91,93번 충전 가능
    """
    ChargingZone = [False, False, False]
    for i in range(1, 21):
        key = f"AMR_STATUS:AMR{i:03}"
        if not r.exists(key):
            continue
        h = r.hgetall(key)
        if str(h.get("missionType")).upper() in ("CHARGING") and "submissionList" in h:
            try:
                submission_list = [json.loads(s) for s in json.loads(h["submissionList"])]
                submission_nodes = [s.get("submissionNode") for s in submission_list]
                if submission_nodes:
                    last_node = int(submission_nodes[-1])
                    if 91 <= last_node <= 93:
                        ChargingZone[last_node - 91] = True
            except Exception:
                pass
    return [i + 91 for i, used in enumerate(ChargingZone) if not used]

def get_charging_assignments():
    available_zones = findAvailableChargingZones()
    max_amrs = len(available_zones)

    batteryList = [
        (int(battery), f"AMR{i:03}")
        for i in range(1, 21)
        if r.exists(key := f"AMR_STATUS:AMR{i:03}")
           and (h := r.hgetall(key))
           and (battery := h.get("battery")) is not None
           and int(battery) <= 50
    ]

    # 배터리 낮은 순으로 정렬 후 상위 max_amrs개 추출
    sorted_amrs = [amr for _, amr in sorted(batteryList)[:max_amrs]]


    return available_zones, sorted_amrs


# -------------------- ① Redis → Python --------------------

def fetch_robot_list(needChargeAmrs,triggered_amr,inputMissionType) -> list[tuple[str, int, int]]:
    robot_list = []
    ban_work_list = []
        
    for i in range(1, 21):
        key = f"AMR_STATUS:AMR{i:03}"
        if not r.exists(key):
            continue
        if f"AMR{i:03}" in needChargeAmrs:
            continue
        
        h = r.hgetall(key)
        amr_id = h.get("amrId", f"AMR{i:03}")
        node_id = int(h.get("currentNode"))  # 기본값
        submission_nodes=[]
        # submissionList가 존재할 때 처리
        if "submissionList" in h:
            try:
                submission_list = [json.loads(s) for s in json.loads(h["submissionList"])]
                # submissionNode 목록만 추출
                submission_nodes = [int(s.get("submissionNode")) for s in submission_list]

                # currentNode가 submissionList에 있다면, 그 다음 submissionNode 사용
                if len(submission_nodes)!=0:
                    node_id = submission_nodes[int(h.get("submissionId", 0))]

            except Exception as e:
                pass
        
        #loading = 1 if str(h.get("missionType", "")).upper() in ("CHARGING") else 0
        loading = 1 if str(h.get("loading", "")).lower() in ("true") else 0
        if not(1<=node_id<=10 or 21<=node_id<=30 or 41<=node_id<=50):
            if not(str(h.get("missionType", "")).upper() in ("CHARGING")):
                robot_list.append((amr_id, node_id, loading))
            else:
                if amr_id==triggered_amr and inputMissionType=="CHARGING":
                    robot_list.append((amr_id,node_id,0))
        else:
            print(f"{amr_id}가 {node_id}가 다음 목적지 이기 떄문에 계산에 추가하지 않습니다.")
            print(f"서브미션 노드는 {submission_nodes} 이고 서브미션 ID는 {int(h.get('submissionId', 0))}입니다.")
            if 1<=node_id<=10:
                ban_work_list.append(int(h.get("finalGoal")))
            else:
                ban_work_list.append(node_id)
        if loading==1:
            ban_work_list.append(int(h.get("finalGoal")))

    ban_work_list.extend(brokenNode)

    return robot_list,ban_work_list
        

def fetch_line_status(banlist) -> list[tuple[int, float]]:
    """
    Redis 키 MISSION_PT:11~50 에 저장된 ISO8601 문자열 → 현재시각과의 차이를 점수로 사용
    """
    now = datetime.now()
    line_status = []
    for node in range(11, 51):
        if node in banlist:
            continue
        key = f"MISSION_PT:{node}"
        if r.exists(key):
            ts_raw = r.get(key)
            ts_str = ts_raw.decode() if isinstance(ts_raw, bytes) else str(ts_raw)
            ts_str = ts_str.strip('"')  # 큰따옴표 제거

            if ts_str == "-1" or ts_str == -1:
                continue

            try:
                ts = datetime.fromisoformat(ts_str)
                elapsed = (now - ts).total_seconds()
                if 11 <= node <= 20 or 31 <= node <= 40:
                    elapsed += api.loadingTimeTable[(node - 1) % 10 + 1][node]
                line_status.append((node, elapsed))
            except Exception as e:
                print(f"⚠️ {key} 값 변환 실패: {ts_str} ({e})")
    return line_status

def build_results_from_assign(assign):
    all_results = []
    
    for (amr_id, _, _), (dest, _), mission_type, path, cost in assign:
        if cost >= 900 or path is None:
            print(cost, " 코스트 넘치거나", path, "경로가 없음")
            continue

        key = f"AMR_STATUS:{amr_id}"
        h = r.hgetall(key)

        if "submissionList" in h:
            raw_value = h["submissionList"]
            raw_list = json.loads(raw_value)

            submission_list = []
            for s in raw_list:
                if isinstance(s, str):
                    try:
                        parsed = json.loads(s)
                        submission_list.append(parsed)
                    except Exception:
                        continue
                elif isinstance(s, dict):
                    submission_list.append(s)
                elif isinstance(s, int):
                    submission_list.append({"submissionNode": s})

            submission_nodes = [s.get("submissionNode") for s in submission_list if s.get("submissionNode") is not None]
            if len(submission_list) != 0:
                submission_nodes = submission_nodes[:int(h.get("submissionId", 0))]
            print(f'이전 경로 :{submission_nodes} , ID:{int(h.get("submissionId", 0))} 알고리즘 경로 :{path}')
            if len(submission_nodes) != 0 and submission_nodes[-1] == path[0]:
                path = submission_nodes[:-1] + path
            else:
                path = submission_nodes + path
            print(f'최종 경로 :{path} , ID:{int(h.get("submissionId", 0))}')

        result = {
            "amrId": amr_id,
            "missionId": f"MISSION{int(dest):03}",
            "missionType": mission_type,
            "route": path,
            "expectedArrival": int(cost)//2
        }
        all_results.append(result)
        print("결과", result)
    
    return all_results


# -------------------- ② 알고리즘 실행 --------------------

def print_assignment(consumer, partitions):
    print("🟢 카프카 연결 완료")




def listen_loop():
    global brokenLine, brokenNode  # 전역 변수 사용 선언
    brokenLine=[]
    brokenNode=[]
    consumer.subscribe(["algorithm-trigger"], on_assign=print_assignment)
    while True:
        msg = consumer.poll(1.0)
        if msg is None or msg.error():
            continue

        raw_value = msg.value().decode("utf-8").strip()
        print(f"📩 수신 메시지: {raw_value}")
        
        # ✅ 케이스 1: "simulator start"
        if raw_value.lower() == "simulator start":
            print("🚀 [Simulator Start] 알고리즘 강제 실행")
            triggered_amr = None
            cancelled_amrs = []
            inputMissionType = "START"

        elif raw_value.startswith("LINE BROKEN : "):
            print("🚀 [LINE BROKEN] 라인 고장")
            brokenLine=[10]
            brokenNode=[20,30]
            result=[]
            for i in range(1,21):
                amr_id=f"AMR{i:03d}"
                key = f"AMR_STATUS:{amr_id}"
                h = r.hgetall(key)
                if "submissionList" in h:
                    try:
                        submission_list = [json.loads(s) for s in json.loads(h["submissionList"])]
                        # submissionNode 목록만 추출
                        submission_nodes = [s.get("submissionNode") for s in submission_list]

                        # currentNode가 submissionList에 있다면, 그 다음 submissionNode 사용
                        if len(submission_nodes)!=0:
                            node_id = submission_nodes[int(h.get("submissionId", 0))]
                        else:
                            node_id = int(h.get("currentNode"))  # 기본값
                            pass  # 그대로 current_node 유지
                    except Exception as e:
                        pass
                if not submission_nodes:
                    continue  # 서브미션 노드가 없는 경우 그냥 건너뛰기
                if submission_nodes[-1]==20:
                    lineBrokenMission=api.calEdgeCutRoute([(node_id,40)],[amr_id])
                    print(f"[LINE BROKEN] {amr_id}를 : 40번으로 유배")
                    result.extend(lineBrokenMission)
                elif submission_nodes[-1]==30:
                    lineBrokenMission=api.calEdgeCutRoute([(node_id,89)],[amr_id])
                    print(f"[LINE BROKEN] {amr_id}를 : 89번으로 유배")
                    result.extend(lineBrokenMission)
            if result:
                all_results = build_results_from_assign(result)
                print("[LINE BROKEN] : 라인 부신거 결과",all_results)
                if all_results:
                    payload = {
                        "triggeredAmr": None,
                        "missions": all_results
                    }
                    producer.produce("algorithm-result", json.dumps(payload))
                    producer.flush()
            continue


        elif raw_value.startswith("LINE REPAIR : "):
            print("🚀 [LINE REPAIR] 라인 수리")
            brokenLine=[]
            brokenNode=[]
            continue

        elif raw_value.lower().startswith("none edge error"):
            try:
                # 예: "None Edge Error : AMR015"
                amr_id = raw_value.split(":")[-1].strip()  # → 'AMR015'
                key = f"AMR_STATUS:{amr_id}"
                h = r.hgetall(key)
                
                assign = api.calEdgeCutRoute([(int(h.get("currentNode")),89)], [amr_id])
                all_results = build_results_from_assign(assign)
                print(f"{amr_id} 는 89번으로 유배 보냄")
                if all_results:
                    payload = {
                        "triggeredAmr": triggered_amr,  # None 일 수도 있음
                        "missions": all_results
                    }
                    producer.produce("algorithm-result", json.dumps(payload))
                    producer.flush()
                

            except Exception as e:
                print(f"❌ 유배 처리 중 오류 발생: {e}")

            continue
        # ✅ 케이스 2: JSON payload
        elif raw_value.startswith("{"):
            try:
                payload = json.loads(raw_value)
            except Exception as e:
                print(f"❌ JSON 파싱 실패: {e}")
                continue

            # ✅ edge cut 처리
            if "cutEdge" in payload:
                print("🚀 [edge cut] 엣지 컷 들어옴")
                cutEdge = int(payload.get("cutEdge") or 0)
                cancelled_amrs = payload.get("cancelledAmrs") or []
                print(f"자를 엣지 {cutEdge} 취소된 AMR {cancelled_amrs}")
                api.mapInit(cutEdge)

                if len(cancelled_amrs)==0:
                    continue

                startCancelStartEndNode = findStartCancelledAmrs(cancelled_amrs)
                print("계산할 시작노드 와 도착지노드", startCancelStartEndNode)
                assign = api.calEdgeCutRoute(startCancelStartEndNode, cancelled_amrs)
                all_results = build_results_from_assign(assign)
                print("자른 엣지 결과",all_results)
                if all_results:
                    payload = {
                        "triggeredAmr": None,
                        "missions": all_results
                    }
                    producer.produce("algorithm-result", json.dumps(payload))
                    producer.flush()
                continue

            # ✅ 일반적인 미션 완료 메시지 처리
            triggered_amr = payload.get("amrId")
            cancelled_amrs = payload.get("cancelledAmrs", [])
            inputMissionType = payload.get("missionType")
            print(f"미션 완료 : {triggered_amr} 미션 타입 : {inputMissionType}")

            if not triggered_amr:
                print("⚠️ triggered_amr 없음 → 알고리즘 실행 생략")
                continue

        # ✅ 예외: 알 수 없는 형식
        else:
            print(f"⚠️ 비정형 메시지 무시됨: {raw_value}")
            continue



        # ✅ 알고리즘 실행 부분 공통

        zones, amrs = get_charging_assignments() # zones : 충전 가능한 구역,amrs : 현재 충전 해야하는 amr 기기 번호
        print(f"충전 구역 : {zones} 충전 해야하는 기기들 : {amrs}")
        robot,banlist   = fetch_robot_list(amrs,triggered_amr,inputMissionType)
        print(f"일 할 로봇 : {robot} 금지구역 : {banlist}")
        jobs    = fetch_line_status(banlist)
        try:
            assign = api.assign_tasks(robot, jobs)
        except Exception as e:
            print(f"⚠️ assign_tasks 실행 중 오류 발생: {e}")
            continue  # 에러가 나면 다음 루프로 넘어감
        """ 충전 친구들도 넣어야함 """

        # 추가 조건 필터링: CHARGING 미션이거나 loading == true인 경우 제외
        charge_amrs = []
        for amr_id in amrs:
            key = f"AMR_STATUS:{amr_id}"
            h = r.hgetall(key)
            mission_type = str(h.get("missionType", "")).upper()
            loading = str(h.get("loading", "")).lower()
            if mission_type == "CHARGING" or loading == "true":
                continue  # 제외 조건
            charge_amrs.append(amr_id)
        chargeStartNode=[]
        for amr_id in charge_amrs:
            key = f"AMR_STATUS:{amr_id}"
            h = r.hgetall(key)
            if str(h.get("loading", "")).lower() in ("true"):
                continue
            if "submissionList" in h:
                try:
                    submission_list = [json.loads(s) for s in json.loads(h["submissionList"])]
                    # submissionNode 목록만 추출
                    submission_nodes = [s.get("submissionNode") for s in submission_list]

                    # currentNode가 submissionList에 있다면, 그 다음 submissionNode 사용
                    if len(submission_nodes)!=0:
                        node_id = submission_nodes[int(h.get("submissionId", 0))]
                    else:
                        node_id = int(h.get("currentNode"))  # 기본값
                        pass  # 그대로 current_node 유지

                except Exception as e:
                    pass
            chargeStartNode.append((node_id,amr_id))

        chargeResult = api.assign_charging_spots(chargeStartNode, zones,amrs)
        
        assign.extend(chargeResult)
        all_results = build_results_from_assign(assign)
        
        print("")
        if all_results:
            payload = {
                "triggeredAmr": triggered_amr,  # None 일 수도 있음
                "missions": all_results
            }
            producer.produce("algorithm-result", json.dumps(payload))
            producer.flush()
            #print(f"📤 Kafka 전송 완료 (trigger: {triggered_amr})")


testNumber=15
if __name__ == "__main__":
    #api.mapInit()
    listen_loop()
