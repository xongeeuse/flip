import simpy.rt
import threading
import time
import math
from collections import deque
import socketio
import eventlet.wsgi
from datetime import datetime
from map_data import map_data


# ---------- 설정 ----------
REALTIME_INTERVAL = 0.01  # 좌표 갱신 주기 (초)
NUM_AMR = 3              # AMR 개수
SHARED_STATUS = {}        # 모든 AMR의 실시간 위치 상태
LOCK = threading.Lock()   # 공유 자원 보호

# ---------- Socket.IO 서버 ----------
sio = socketio.Server(cors_allowed_origins='*',  async_mode='threading')
app = socketio.WSGIApp(sio)

# ---------- AMR 클래스 ----------
class AMR:
    def __init__(self, env, amr_id, map_data, pos_x, pos_y, type):
        self.env = env
        self.id = amr_id
        self.map_data = map_data
        self.pos_x = pos_x
        self.pos_y = pos_y
        self.dir = 0  # 방향(degree)
        self.state = 1  # 1: IDLE, 2: PROCESSING
        self.battery = 100

        self.mission_queue = deque()
        self.current_mission = None
        self.interrupt_flag = False

        self.current_mission_id = None
        self.current_mission_type = None
        self.current_submission_id = None
        self.current_speed = 0
        self.loaded = False

        self.current_node_id = None
        self.type = type

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
                "type" : self.type
            }

    def assign_mission(self, mission, replace=False):
        if replace:
            self.interrupt_flag = True
            self.mission_queue.clear()
        self.mission_queue.append(mission)

    def run(self):
        while True:
            if self.interrupt_flag:
                self.interrupt_flag = False
                self.current_mission = None

            if not self.current_mission and self.mission_queue:
                self.current_mission = self.mission_queue.popleft()
                yield from self.process_mission(self.current_mission)
                self.current_mission = None
            else:
                yield self.env.timeout(REALTIME_INTERVAL)

    def process_mission(self, mission):
        self.state = 2
        self.current_mission_id = mission["missionId"]
        self.current_mission_type = mission["missionType"]
        self.update_status()
        for sub in mission["submissions"]:
            self.current_submission_id = sub["submissionId"]
            node_id = sub["nodeId"]
            node = self.map_data["nodes"][sub["nodeId"]]
            edge = self.map_data["edges"][sub["edgeId"]]
            self.current_speed = edge["speed"]
            yield from self.move_to_node(node_id, node, edge)
        self.state = 1
        self.current_mission_id = None
        self.current_mission_type = None
        self.current_submission_id = None
        self.current_speed = 0
        self.update_status()

    def move_to_node(self, node_id, node, edge):
        # 현재 위치와 목표 노드까지의 거리·속도·스텝 계산
        distance = self.get_distance(self.pos_x, self.pos_y, node["x"], node["y"])
        speed = edge["speed"]  # m/s
        duration = distance / speed  # 전체 이동 시간 (초)
        steps = max(1, int(duration / REALTIME_INTERVAL))
        dx = (node["x"] - self.pos_x) / steps
        dy = (node["y"] - self.pos_y) / steps

        # 1) 표준 각도 (X축 기준, 반시계 +)
        angle_rad = math.atan2(dy, dx)
        angle_std = math.degrees(angle_rad) % 360

        # 2) Y축+을 0°, X축+을 90°로 매핑하고 시계 방향을 +로
        #    → target_dir이 0°일 때 Y양수, 90°일 때 X양수
        target_dir = (90 - angle_std) % 360

        # 3) 현재 방향(self.dir)과 목표 방향 차이 계산 (±180°)
        diff = (target_dir - self.dir + 360) % 360
        if diff > 180:
            diff -= 360

        # 4) 회전하기 (3초에 360° 회전)
        turn_speed = 360 / 3  # degrees per second
        turn_per_step = turn_speed * REALTIME_INTERVAL
        steps_to_turn = int(abs(diff) / turn_per_step)

        for _ in range(steps_to_turn):
            yield self.env.timeout(REALTIME_INTERVAL)
            # diff > 0 → 시계 방향(+), diff < 0 → 반시계 방향(−)
            self.dir = (self.dir + turn_per_step * (1 if diff > 0 else -1)) % 360
            self.update_status()

        # 5) 정확히 목표 방향으로 스냅
        self.dir = target_dir
        self.update_status()

        # 6) 직선 이동
        for _ in range(steps):
            yield self.env.timeout(REALTIME_INTERVAL)
            self.pos_x += dx
            self.pos_y += dy

            # 배터리 소모
            self.battery = max(self.battery - 0.0001, 0)
            self.update_status()

        # 7) 최종 위치 보정
        self.pos_x = node["x"]
        self.pos_y = node["y"]
        self.current_node_id = node_id
        self.update_status()

    def get_distance(self, x1, y1, x2, y2):
        return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)

# ---------- AMR 초기화 ----------
def setup_amrs(env, map_data):
    amrs = []

    amr = AMR(env, f"AMR001", map_data, 2.5, 17.5, 0)
    amr.update_status()  # ✅ 최초 상태 넣기
    env.process(amr.run())
    amrs.append(amr)

    amr = AMR(env, f"AMR002", map_data, 5.5, 17.5, 1)
    amr.update_status()  # ✅ 최초 상태 넣기
    env.process(amr.run())
    amrs.append(amr)

    amr = AMR(env, f"AMR003", map_data, 8.5, 17.5, 2)
    amr.update_status()  # ✅ 최초 상태 넣기
    env.process(amr.run())
    amrs.append(amr)

    return amrs


# ---------- 미션 보내기 ----------
def send_missions(amrs):
    for i, amr in enumerate(amrs):

        if amr.id.endswith("1"):
            # AMR001, AMR002는 왼쪽 코스로
            amr.assign_mission({
                "missionId": 1,
                "missionType": "MOVING",
                "submissions": [
                    {"submissionId": "1", "nodeId": "12", "edgeId": "7"},
                    {"submissionId": "2", "nodeId": "13", "edgeId": "8"},
                    {"submissionId": "3", "nodeId": "2", "edgeId": "4"},
                    {"submissionId": "4", "nodeId": "14", "edgeId": "5"},
                    {"submissionId": "5", "nodeId": "20", "edgeId": "19"},
                    {"submissionId": "6", "nodeId": "4", "edgeId": "35"},
                    {"submissionId": "7", "nodeId": "21", "edgeId": "21"},
                    {"submissionId": "8", "nodeId": "27", "edgeId": "37"},
                    {"submissionId": "9", "nodeId": "6", "edgeId": "44"},
                    {"submissionId": "10", "nodeId": "26", "edgeId": "43"},
                    {"submissionId": "11", "nodeId": "32", "edgeId": "57"},
                    {"submissionId": "12", "nodeId": "8", "edgeId": "71"},
                ]
            })
        elif amr.id.endswith("2"):
            # 나머지 AMR은 오른쪽 코스로
            amr.assign_mission({
                "missionId": 2,
                "missionType": "MOVING",
                "submissions": [
                    {"submissionId": "1", "nodeId": "11", "edgeId": "7"},
                    {"submissionId": "2", "nodeId": "17", "edgeId": "12"},
                    {"submissionId": "3", "nodeId": "23", "edgeId": "29"},
                    {"submissionId": "4", "nodeId": "24", "edgeId": "45"},
                    {"submissionId": "5", "nodeId": "5", "edgeId": "39"},
                    {"submissionId": "6", "nodeId": "25", "edgeId": "40"},
                    {"submissionId": "7", "nodeId": "31", "edgeId": "54"},
                    {"submissionId": "8", "nodeId": "7", "edgeId": "68"},
                    {"submissionId": "9", "nodeId": "32", "edgeId": "70"},
                    {"submissionId": "10", "nodeId": "33", "edgeId": "65"},
                    {"submissionId": "11", "nodeId": "34", "edgeId": "66"},
                    {"submissionId": "12", "nodeId": "28", "edgeId": "61"},
                    {"submissionId": "13", "nodeId": "22", "edgeId": "38"},
                    {"submissionId": "14", "nodeId": "16", "edgeId": "23"},
                    {"submissionId": "15", "nodeId": "15", "edgeId": "11"},
                    {"submissionId": "16", "nodeId": "2", "edgeId": "6"},
                ]
            })
        elif amr.id.endswith("3"):
            amr.assign_mission({
                "missionId": 3,
                "missionType": "MOVING",
                "submissions": [
                    {"submissionId": "1", "nodeId": "2", "edgeId": "4"},
                    {"submissionId": "2", "nodeId": "14", "edgeId": "5"},
                    {"submissionId": "3", "nodeId": "20", "edgeId": "19"},
                    {"submissionId": "4", "nodeId": "4", "edgeId": "35"},
                    {"submissionId": "5", "nodeId": "19", "edgeId": "33"},
                    {"submissionId": "6", "nodeId": "13", "edgeId": "16"},
                    {"submissionId": "7", "nodeId": "1", "edgeId": "2"},
                    {"submissionId": "8", "nodeId": "13", "edgeId": "2"},
                    {"submissionId": "9", "nodeId": "19", "edgeId": "16"},
                    {"submissionId": "10", "nodeId": "3", "edgeId": "32"},
                    {"submissionId": "11", "nodeId": "18", "edgeId": "31"},
                    {"submissionId": "12", "nodeId": "17", "edgeId": "24"},
                    {"submissionId": "13", "nodeId": "23", "edgeId": "29"},
                    {"submissionId": "14", "nodeId": "24", "edgeId": "45"},
                    {"submissionId": "15", "nodeId": "25", "edgeId": "46"},
                    {"submissionId": "16", "nodeId": "6", "edgeId": "42"},
                ]
            })

# ---------- 상태 전송 ----------
def broadcast_status():
    while True:
        with LOCK:
            if not SHARED_STATUS:
                time.sleep(0.1)
                continue

            now = datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
            messages = []
            for amr_id, status in SHARED_STATUS.items():
                message = {
                    "header": {
                        "msgName": "AGV_STATE",
                        "time": now
                    },
                    "body": {
                        "amrId": status["id"],
                        "state": status["state"],
                        "locationX": status["x"],
                        "locationY": status["y"],
                        "type": status["type"],
                        "dir": status["dir"],
                        "currentNode": status.get("currentNode", ""),
                        "loading": "1" if status["loaded"] else "0",
                        "linearVelocity": status.get("speed", 0),
                        "battary": status["battery"],
                        "errorList": [],
                        "missionId": status.get("missionId", ""),
                        "submissionId": status.get("submissionId", "")
                    }
                }
                messages.append(message)

            if messages:
                # print(f"[Broadcast] {len(messages)} AMRs - example:", messages[0])  # ✅ 추가
                print("dir : ", messages[0]["body"]["dir"])
            sio.emit('amr_status', messages)

        time.sleep(0.1)



# ---------- 메인 ----------
if __name__ == '__main__':
    env = simpy.rt.RealtimeEnvironment(factor=1.0, strict=False)


    amrs = setup_amrs(env, map_data)
    send_missions(amrs)

    threading.Thread(target=broadcast_status, daemon=True).start()
    threading.Thread(target=lambda: env.run(), daemon=True).start()

    eventlet.wsgi.server(eventlet.listen(('0.0.0.0', 5000)), app)
