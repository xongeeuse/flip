import api
result=[]
amr_id="AMR006"
def calEdgeCutRoute(startCancelStartEndNode,cancelled_amrs):
    results = []
    for i in range(len(startCancelStartEndNode)):
        amrId=cancelled_amrs[i]
        start=startCancelStartEndNode[i][0]
        dest=startCancelStartEndNode[i][1]
        path,cost=api.aStar(start,dest)
        best_path = [node for node, _ in path]
        results.append(((amrId, "dummy", "dummy"), (dest, "dummy"), api.missions[dest], best_path, cost))
    return results

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

lineBrokenMission=calEdgeCutRoute([(10,40)],[amr_id])
print(f"[LINE BROKEN] {amr_id}를 : 40번으로 유배")
result.extend(lineBrokenMission)
print(result)
if result:
    all_results = build_results_from_assign(result)
    print("[LINE BROKEN] : 라인 부신거 결과",all_results)
    if all_results:
        payload = {
            "triggeredAmr": None,
            "missions": all_results
        }