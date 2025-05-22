from scipy.optimize import linear_sum_assignment
import heapq
import json
import random
import numpy as np
import bisect
import pymysql
import math
import os
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 환경 변수에서 DB 정보 가져오기
DB_HOST = os.getenv('DB_HOST',"localhost")
DB_PORT = int(os.getenv('DB_PORT',"3307"))
DB_USER = os.getenv('DB_USER',"root")
DB_PASSWORD = os.getenv('DB_PASSWORD',"1234")
DB_NAME = os.getenv('DB_NAME',"flip")

# DB 연결
conn = pymysql.connect(
    host=DB_HOST,
    port=DB_PORT,
    user=DB_USER,
    password=DB_PASSWORD,
    database=DB_NAME,
    charset='utf8mb4',
    cursorclass=pymysql.cursors.DictCursor
)


def mapInit(banEdge=0):
    global nodes, edges, graph, missions
    nodes = {}       # ✅ 누락된 초기화
    edges = []       # ✅ 누락된 초기화
    graph = {}       # 기존에도 했던 초기화
    missions = {}    # ✅ 누락된 초기화
    with conn.cursor() as cursor:
        # ① Node 정보
        cursor.execute("SELECT node_id, x, y FROM node")
        node_rows = cursor.fetchall()
        for row in node_rows:
            nodes[row['node_id']] = {
                'x': row['x'],
                'y': row['y']
            }

        # ② Edge 정보
        cursor.execute("""
            SELECT edge_id,edge_direction, speed, node1_node_id AS node1, node2_node_id AS node2
            FROM edge
        """)
        edge_rows = cursor.fetchall()
        for edge in edge_rows:
            edges.append(edge)

        # ③ Mission 정보 (추가)
        cursor.execute("""
            SELECT mission_id, mission_type, target_node_id
            FROM mission
        """)
        mission_rows = cursor.fetchall()
        for row in mission_rows:
            missions[row['target_node_id']] = row['mission_type']

    # ④ 그래프 생성
    for edge in edges:
        if edge['edge_id'] == banEdge:
            print(f"{banEdge} 삭제함")
            continue
        node1 = edge['node1']
        node2 = edge['node2']
        direction = edge['edge_direction'].lower()
        speed = edge['speed']

        x1, y1 = nodes[node1]['x'], nodes[node1]['y']
        x2, y2 = nodes[node2]['x'], nodes[node2]['y']
        distance = math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)
        weight = distance / speed

        if direction == 'twoway':
            graph.setdefault(node1, []).append((node2, weight))
            graph.setdefault(node2, []).append((node1, weight))
        elif direction == 'forward':
            graph.setdefault(node1, []).append((node2, weight))
        elif direction == 'rearward':
            graph.setdefault(node2, []).append((node1, weight))
        else:
            raise ValueError(f"Unknown direction: {direction}")


def aStar(start, end, start_time=0):
    if end >= 1000:
        return None, 0

    open_set = []  # (f_score, time, node)
    heapq.heappush(open_set, (0, start_time, start, []))  # (f, t, node, path)

    visited = set()

    while open_set:
        f, t, node, path = heapq.heappop(open_set)
        if (node, t) in visited:
            continue
        visited.add((node, t))

        path = path + [(node, t)]

        if node == end:
            total_cost = t - start_time
            return path, total_cost

        for neighbor, weight in graph.get(node, []):
            next_time = t + math.ceil(weight)
            g = next_time - start_time
            h = heuristic(neighbor, end)
            heapq.heappush(open_set, (g + h, next_time, neighbor, path))

    return None, float('inf')


# 휴리스틱 함수 (유클리드 거리)
def heuristic(node1, node2):
    x1, y1 = nodes[node1]['x'], nodes[node1]['y']
    x2, y2 = nodes[node2]['x'], nodes[node2]['y']
    return math.sqrt((x2 - x1)**2 + (y2 - y1)**2)

# ---------- 작업별 적재(1) / 비적재(0) 요구 ----------
def needs_load(task_id: int) -> bool:
    return 11 <= task_id <= 20 or 31 <= task_id <= 40          # 적재 必

def needs_unload(task_id: int) -> bool:
    return 21 <= task_id <= 30 or 41 <= task_id <= 50          # 비적재 必


# ---------- 로봇‑작업 1쌍에 대한 비용·경로 계산 ----------

def calc_cost_and_route(start_node: int,task_dest: int):
    if needs_load(task_dest):
        return min(MovingTimeTable[start_node][i] + loadingTimeTable[i+1][task_dest] for i in range(10))
    elif needs_unload(task_dest):
        if 21<=task_dest<=30:
            return MovingTimeTable[start_node][task_dest-11]
        elif 41<=task_dest<=50:
            return MovingTimeTable[start_node][task_dest-21]
        else:
            print("오류 적재 반납 미션 이외의 목적지 : ", task_dest)
    elif task_dest==80:
        return 0
    else:
        print("알수 없는 목적지 : ", task_dest)

def extract_unique_nodes_nonconsecutive(path):
    result = []
    prev_node = None
    for node, _ in path:
        if node != prev_node:
            result.append(node)
            prev_node = node
    return result



# ---------- 헝가리안 알고리즘 ----------
def hungarian(robotList, taskList):
    robotList = [robot for robot in robotList if robot[2] == 0]
    n, m = len(robotList), len(taskList)
    if n > m:                                # 더미 작업 보충
        for k in range(n - m):
            taskList.append((80, 0))
    taskList = taskList[:n]

    cost_matrix = np.zeros((n, n))

    for i in range(n):
        _,start_node, loaded = robotList[i]     # 위치, 적재 여부
        for j in range(n):
            dest, _ = taskList[j]
            cost_matrix[i][j] = calc_cost_and_route(start_node, dest)

    # 헝가리안 수행 (불가능 매칭은 ∞ 비용으로 자동 제외)
    row_ind, col_ind = linear_sum_assignment(cost_matrix)

    # 매칭 결과를 점수 기준으로 정렬
    assignments = sorted([(taskList[j][1], i, j) for i, j in zip(row_ind, col_ind)],reverse=True)
    total = 0
    LoadingStartTime=[[-10] for _ in range(11)]
    aStarResult=[[] for _ in range(n)]
    for score, i, j in assignments:
        robot_name, robot_id, loaded = robotList[i]
        dest, _ = taskList[j]
        start_node = robot_id

        if math.isinf(cost_matrix[i][j]):
            continue

        final_path = []
        total_cost = 0
        

        if needs_load(dest):
            best_p, best_ready, best_done = None, None, float('inf')
            for p in range(1,11):
                arrival = MovingTimeTable[start_node][p-1]
                waitTime = 0
                storageTime = MovingTimeTable[start_node][p-1]
                for absTime in LoadingStartTime[p]:
                    if absTime<=storageTime<absTime+10:
                        storageTime=absTime+10
                    elif storageTime<absTime<storageTime+10:
                        storageTime=absTime+10
                waitTime=storageTime-arrival
                done = arrival+waitTime + loadingTimeTable[p][dest] # 목적지까지

                if done < best_done:
                    best_p, best_start,best_ready, best_done = p, arrival+waitTime,waitTime, done # 적재 포트,적재 시작 시간,대기시간,끝나는시간

            # 스케줄 기록
            bisect.insort(LoadingStartTime[best_p],best_start)

            #print(f"📦 로봇 {robot_name} → 포트 {best_p} "f"({best_start}s 적재 시작, {best_ready}s 대기)")
            to_pickup, c1 = aStar(start_node, best_p)
            to_dest,   c2 = aStar(best_p, dest, c1+EXTRA_PICKUP_COST)
            final_path, total_cost = to_pickup + to_dest, c1 + c2

        elif needs_unload(dest):
            to_dest, c1 = aStar(start_node, dest)
            to_next, c2 = aStar(dest, ((dest - 1) % 10 + 1) + 50, c1 if to_dest else 0)
            if to_dest and to_next:
                final_path = to_dest + to_next
                total_cost = c1 + c2
            else:
                print(f"❌ 비적재 경로 없음: {start_node} → {dest} → {((dest - 1) % 10 + 1) + 50}")
                continue

        else:
            final_path, total_cost = aStar(start_node, dest)
        total+=total_cost
        aStarResult[i].append(extract_unique_nodes_nonconsecutive(final_path))
        aStarResult[i].append(total_cost)
        # print(f"🛣️ 경로: {[(n, t) for n, t in final_path]}")
        # print(f"🦾 로봇{robot_name} {start_node} → {dest} | 총 A* 비용: {total_cost:.2f} | 점수 {score}")

    #print(f"\n✅ 총 거리 비용: {total:.2f}")
    return [(robotList[i], taskList[j],missions[dest],aStarResult[i][0], aStarResult[i][1])
            for i, j in zip(row_ind, col_ind)], total

def assign_charging_spots(chargeStartNode, zones,amrs):
    assigned_zones = []
    results = []

    for index,(start,amrId) in enumerate(chargeStartNode):
        if 1<=start<=10 or 21<=start<=30 or 41<=start<=50:
            continue
        best_path = None
        best_time = float('inf')
        best_zone = None

        for zone in zones:
            if zone in assigned_zones:
                continue

            path, cost = aStar(start, zone)
            if cost < best_time:
                best_path = path
                best_time = cost
                best_zone = zone

        if best_zone is not None:
            assigned_zones.append(best_zone)
            node_only_best_path = [node for node, _ in best_path]
            #amrID,목적지,미션내용,최적경로,소요시간간
            results.append(((amrId, "dummy", "dummy"), (best_zone, "dummy"), missions[best_zone], node_only_best_path, best_time))

    return results

def calEdgeCutRoute(startCancelStartEndNode,cancelled_amrs):
    results = []
    for i in range(len(startCancelStartEndNode)):
        amrId=cancelled_amrs[i]
        start=startCancelStartEndNode[i][0]
        dest=startCancelStartEndNode[i][1]
        path,cost=aStar(start,dest)
        best_path = [node for node, _ in path]
        results.append(((amrId, "dummy", "dummy"), (dest, "dummy"), missions[dest], best_path, cost))
    return results


# --- 맨 아래의 “demo 코드” 삭제 -----------------------------
# robot = 20
# ...
# result = hungarian(robotList, taskList)
# -----------------------------------------------------------

# 대신 함수로 노출
def assign_tasks(robot_list: list[tuple[str, int, int]],
                 line_status: list[tuple[int, float]]):
    """
    robot_list  = [(amrId, currentNode, loading(0/1)), ...]
    line_status = [(nodeId, score), ...]   # 점수 = 남은 작업 ‘긴급도’ 등
    """
    # score 기준 내림차순 정렬
    task_list = sorted(line_status, key=lambda x: x[1], reverse=True)

    assignments, _ = hungarian(robot_list, task_list)
    return assignments            # [(robotTuple, taskTuple, path, cost), ...]


nodes = {}
edges = []
graph = {}
missions = {}  # ✅ 미션 추가
loadingTimeTable=[[0 for _ in range(41)] for _ in range(11)] # 1~10번 노드가 자재 놓는곳 까지 최적 시간
MovingTimeTable=[[0 for _ in range(30)] for _ in range(233)] # 현재 노드에서 자재없는 미션 가는 곳 까지 최적 시간

# #맵 생성
mapInit()
#astar알고리즘(현재 가고있는 노드,시작노드,끝점)
for start in range(1,11):
    for end in range(11,21):
        loadingTimeTable[start][end]=aStar(start,end,0)[1]
    for end in range(31,41):
        loadingTimeTable[start][end]=aStar(start,end,0)[1]
for start in range(233):
    for end in range(10):
        MovingTimeTable[start][end]=aStar(start,end+1,0)[1]
        MovingTimeTable[start][end+10]=aStar(start,end+21,0)[1]
        MovingTimeTable[start][end+20]=aStar(start,end+41,0)[1]


EXTRA_PICKUP_COST = 10                 # 적재 소요 시간(코스트)
print("✅ 그래프 및 미션 정보 로딩 완료")



# #현재 가동 가능한 로봇수
# robot=20
# excluded_ids = {204, 205, 212, 213, 220, 221, 228, 229}
# robot_candidates = [i for i in list(range(1, 61)) + list(range(101, 233)) if i not in excluded_ids]
# #(현재 위치 , 0은 적재안함 1은 적재상태 , 
# robotList = [(robotName,rid,random.randint(0,1)) for robotName,rid in enumerate(random.sample(robot_candidates, k=robot))]
# lineStatus = random.sample(range(1, 61), 40)
# taskList = [(idx+11,val) for idx,val in enumerate(lineStatus) if val >= 38]
# taskList.sort(key=lambda x: x[1], reverse=True)
# #로봇의 현재 노드 위치[20개], 일의의
# assign,_=hungarian(robotList,taskList)
# all_results = []  # ✅ 전체 결과 리스트 초기화

# for (amr_id, _, _), (dest, _), type, path, cost in assign:
#     if cost == 0 or path is None:
#         continue
#     result = {
#         "amrId"  : amr_id,
#         "missionId": dest,
#         "missionType" : type, #미션 타입 "MOVING", "CHARGING"...
#         "route"  : path,
#         "expectedArrival" : int(cost)
#     }
#     all_results.append(result)
# print(all_results)
