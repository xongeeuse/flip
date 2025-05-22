package com.ssafy.flip.domain.connect.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ssafy.flip.domain.connect.dto.request.RouteTempDTO;
import com.ssafy.flip.domain.connect.service.AlgorithmResultConsumer;
import com.ssafy.flip.domain.connect.service.AlgorithmTriggerProducer;
import com.ssafy.flip.domain.connect.service.WebSocketService;
import com.ssafy.flip.domain.line.service.LineService;
import com.ssafy.flip.domain.log.service.mission.MissionLogService;
import com.ssafy.flip.domain.mission.dto.MissionResponse;
import com.ssafy.flip.domain.status.dto.request.AmrSaveRequestDTO;
import com.ssafy.flip.domain.status.dto.request.LineSaveRequestDTO;
import com.ssafy.flip.domain.status.service.StatusService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmrWebSocketHandler extends TextWebSocketHandler {

    @Getter
    private final Map<String, WebSocketSession> amrSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketService webSocketService;
    private final StatusService statusService;
    private final MissionLogService missionLogService;
    private final AlgorithmTriggerProducer trigger;   // ← Kafka로 트리거

    private final Map<String, Integer> lastSubmissionMap = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> submissionStartMap = new ConcurrentHashMap<>();
    private final Map<String, List<RouteTempDTO>> routeTempMap = new ConcurrentHashMap<>();

    private final Map<Integer, String> nodeOccupants = new ConcurrentHashMap<>();
    private final Map<Integer, Queue<String>> nodeQueues = new ConcurrentHashMap<>();
    private final Map<String, String> lastMissionMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> previousNodeMap = new ConcurrentHashMap<>();

    private final Map<String, Long> missionToLineId = new HashMap<>();

    private static final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final AlgorithmResultConsumer algorithmResultConsumer;

    private final Map<Integer, Object>  nodeLocks     = new ConcurrentHashMap<>();

    private final ThreadPoolTaskExecutor amrTaskExecutor;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<String, Integer> missionToLine = new HashMap<>();
    private final LineService lineService;
    private final StringRedisTemplate stringRedisTemplate;


    @PostConstruct
    public void initObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @PostConstruct
    public void initMissionMapping() {
        for(int i = 11; i <= 20; i++){
            missionToLine.put("MISSION0"+i, i-10);
        }
        for(int i = 31; i <= 40; i++){
            missionToLine.put("MISSION0"+i, i-20);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("AMR 연결 : {}" ,session.getId());
        // WebSocketServiceImpl에서 JSON 데이터 가져옴
        String mapInfoJson = webSocketService.sendMapInfo();
        session.sendMessage(new TextMessage(mapInfoJson));

        // 직접 WebSocket 세션에 메시지 전송
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(mapInfoJson));
            log.info("✅ Map Info 전송 완료: {}" , mapInfoJson);
        } else {
            log.error("❌ WebSocket 세션이 닫혀 있음: {}", session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        // ① 즉시 스레드풀에 위임
        amrTaskExecutor.execute(() -> {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(payload, Map.class);
                String msgName = (String)((Map<?, ?>)jsonMap.get("header")).get("msgName");
                switch (msgName) {
                    case "AMR_STATE":
                        handleAmrState(jsonMap, session);
                        break;
                    case "TRAFFIC_REQ":
                        handleTrafficRequest(jsonMap, session);
                        break;
                    case "SIMULATION_START":
                        handleStimulatorStart();
                        break;
                    case "MISSION_REQ":
                    default:
                        log.warn("Unknown message: {}", msgName);
                }
            } catch (Exception ex) {
                log.error("비동기 메시지 처리 중 에러", ex);
            }
        });
        // ② I/O 스레드는 즉시 반환 → 다음 메시지 수신 대기
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("AMR 연결 종료: {}", session.getId());

        // 연결이 끊긴 session과 매칭되는 amrId를 찾기
        String disconnectedAmrId = null;
        for (Map.Entry<String, WebSocketSession> entry : amrSessions.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                disconnectedAmrId = entry.getKey();
                break;
            }
        }

        if (disconnectedAmrId != null) {
            amrSessions.remove(disconnectedAmrId);
            lastSubmissionMap.remove(disconnectedAmrId);
            submissionStartMap.remove(disconnectedAmrId);
            routeTempMap.remove(disconnectedAmrId);
            log.info("🧹 AMR 데이터 정리 완료: {}", disconnectedAmrId);
        }
    }

    private void handleStimulatorStart(){
        String kafkaPayload = "Simulator Start";
        try {
            Thread.sleep(1000); // 1초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 플래그 복구
            e.printStackTrace();
        }
        trigger.run(kafkaPayload);  // Kafka: algorithm-trigger, 메시지는 전체 AMR 상태
    }

    private void handleAmrState(Map<String, Object> json, WebSocketSession session) {
        try {
            // — 공통 세션·DTO 변환 —
            Map<String, Object> body = (Map<String, Object>) json.get("body");
            String amrId = (String) body.get("amrId");
            amrSessions.put(amrId, session);
            webSocketService.registerSession(amrId, session);

            AmrSaveRequestDTO amrDto = objectMapper.convertValue(json, AmrSaveRequestDTO.class);
            String missionId      = amrDto.body().missionId();
            int    currentSub     = amrDto.body().submissionId();
            int    nodeId         = amrDto.body().currentNode();
            int    edgeId         = amrDto.body().currentEdge();
            int    state          = amrDto.body().state(); // 1=IDLE,2=BUSY

            // — 서브미션 변화 기록 —
            LocalDateTime now = LocalDateTime.now();
            Integer lastSub = lastSubmissionMap.get(amrId);
            if (lastSub != null && !lastSub.equals(currentSub)) {
                routeTempMap
                        .computeIfAbsent(amrId, k -> new ArrayList<>())
                        .add(RouteTempDTO.builder()
                                .missionId(missionId)
                                .submissionId(lastSub)
                                .nodeId(nodeId)
                                .edgeId(edgeId)
                                .startedAt(submissionStartMap.getOrDefault(amrId, now))
                                .endedAt(now)
                                .build()
                        );
                submissionStartMap.put(amrId, now);
                lastSubmissionMap.put(amrId, currentSub);
            } else if (lastSub == null) {
                submissionStartMap.put(amrId, now);
                lastSubmissionMap.put(amrId, currentSub);
            }

            // — 수행 중인(Non-IDLE) 경우 마지막 미션 ID 저장 —
            if (missionId != null) {
                lastMissionMap.put(amrId, missionId);
            }
            // — IDLE이 아니면 상태 저장 & 트래픽 제어 계속 —
            // 1) client에 넘길 route list JSON 생성
            List<String> routeListJson = routeTempMap
                    .getOrDefault(amrId, Collections.emptyList())
                    .stream()
                    .map(r -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("routeId",   r.getSubmissionId());
                        m.put("routeNode", r.getNodeId());
                        m.put("startAt",   r.getStartedAt().format(fmt));
                        try {
                            return objectMapper.writeValueAsString(m);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            statusService.saveAmr(amrDto, routeListJson);

            // 2) 이전 노드 해제 → 대기열 있는 AMR에 퍼밋 전송
            Integer currNode = nodeId;

            if (amrId.equals(nodeOccupants.get(currNode))) {
                // 1) 해당 노드 해제
                nodeOccupants.remove(currNode);

                // 2) 대기열에서 다음 AMR 꺼내 permit 전송
                Queue<String> q = nodeQueues.get(currNode);
                if (q != null && !q.isEmpty()) {
                    String nextAmr = q.poll();
                    nodeOccupants.put(currNode, nextAmr);

                    int nextSub     = lastSubmissionMap.get(nextAmr);
                    String nextMis  = lastMissionMap.get(nextAmr);
                    WebSocketSession nextSession = amrSessions.get(nextAmr);

                    sendTrafficPermit(nextAmr, nextMis, nextSub, currNode, nextSession);
                }
            }
            // — IDLE 전환 시 “미션 완료” 처리 —
            List<RouteTempDTO> temps = routeTempMap.get(amrId);
            if (state == 1 && temps != null && !temps.isEmpty()) {
                log.info("🏁 AMR 미션 완료 감지: {} → {} {}", amrId, missionId, temps);

                // 1) DB에 미션 로그 저장
                missionLogService.saveWithRoutes(amrId, missionId, temps);

                // 2) 지연 맵에 쌓인 미션이 있으면 우선 실행
                MissionResponse delayed = algorithmResultConsumer.getDelayedMissionMap().get(amrId);
                if (delayed != null) {
                    //log.info("🏁 2번째 미션 시작 : {}", amrId);
                    //REdis에 미션 시간을 저장하자
                    log.info(" 2번쨰 미션 첫번쨰 값 {} -> {}",delayed.getRoute().getFirst(),delayed.getRoute().getLast());
                    if ((11<=delayed.getRoute().getLast() && delayed.getRoute().getLast()<=20) ||(31<=delayed.getRoute().getLast() && delayed.getRoute().getLast()<=40)){
                        lineService.updateMissionAssignment(String.valueOf(delayed.getRoute().getLast()));
                    }
                    else if ((21<=delayed.getRoute().getFirst() && delayed.getRoute().getFirst()<=30) ||(41<=delayed.getRoute().getFirst() && delayed.getRoute().getFirst()<=50)){
                        lineService.updateMissionAssignment(String.valueOf(delayed.getRoute().getFirst()));
                    }

                    algorithmResultConsumer.processMission(delayed);
                    
                    algorithmResultConsumer.getDelayedMissionMap().remove(amrId);
                    //log.info("🚀 2번째 미션 실행 완료: {}", amrId);

                    algorithmResultConsumer.sendWebTrigger();
                }
                // 3) 아니면 일반 상태 트리거
                else {
                    lineService.markMissionBlockedNow(missionId);
                    List<String> cancelledAmrs = new ArrayList<>();

                    // missionType을 UNLOADING으로 덮어쓰기
                    String amrKey = "AMR_STATUS:" + amrDto.body().amrId();
                    //stringRedisTemplate.opsForHash().put(amrKey, "missionType", "LOADING");
                    stringRedisTemplate.opsForHash().put(amrKey, "submissionList", "[]");
                    //미션 취소 뿌리기
                    for (int i = 1; i <= 20; i++) {
                        String amrCancelId = String.format("AMR%03d", i);
                        String amrCancelKey = "AMR_STATUS:" + amrCancelId;

                        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(amrCancelKey);

                        Object loadingObj = map.get("loading");
                        if (loadingObj != null && "false".equalsIgnoreCase(loadingObj.toString())) {

                            boolean shouldCancel = true;  // 기본은 취소

                            // ✅ 예외 1: missionType == "CHARGING"
                            String missionType = (map.getOrDefault("missionType", "")).toString();
                            if ("CHARGING".equalsIgnoreCase(missionType)) {
                                shouldCancel = false;
                            }

                            // ✅ 예외 2,3: submissionList 조건
                            int submissionId = 0;
                            try {
                                submissionId = Integer.parseInt(map.getOrDefault("submissionId", "0").toString());
                            } catch (Exception e) {
                                // 파싱 실패 → 기본 shouldCancel 유지
                            }

                            Object listRaw = map.get("submissionList");
                            if (listRaw == null) continue;

                            List<Map<String, Object>> parsedList = new ArrayList<>();
                            try {
                                List<String> rawList = new ObjectMapper().readValue(listRaw.toString(), List.class);
                                for (String item : rawList) {
                                    parsedList.add(new ObjectMapper().readValue(item, Map.class));
                                }
                            } catch (Exception e) {
                                continue;
                            }

                            int listSize = parsedList.size();

                            // 예외 2: 마지막 submission이면 취소하지 않음
                            if (submissionId == listSize) {
                                shouldCancel = false;
                            }

                            // 예외 3: 특정 submissionNode 범위면 취소하지 않음
                            else if (submissionId > 0 && submissionId <= listSize) {
                                try {
                                    int node = Integer.parseInt(parsedList.get(submissionId).get("submissionNode").toString());
                                    if ((1 <= node && node <= 10) || (21 <= node && node <= 30) || (41 <= node && node <= 50)) {
                                        shouldCancel = false;
                                    }
                                } catch (Exception e) {
                                    // node 파싱 실패 시 무시
                                }
                            }

                            // ✅ 최종 취소
                            if (shouldCancel) {
                                webSocketService.sendCancelMission(amrCancelId);
                                cancelledAmrs.add(amrCancelId);
                            }
                        }
                    }


                    Map<String, Object> payloadMap = new HashMap<>();
                    payloadMap.put("amrId", amrDto.body().amrId());
                    payloadMap.put("cancelledAmrs", cancelledAmrs);
                    payloadMap.put("missionType", amrDto.body().missionType());

                    String payload = objectMapper.writeValueAsString(payloadMap);
                    trigger.run(payload);


                    if (missionToLine.containsKey(missionId)) {
                        statusService.saveLine(missionToLine.get(missionId));
                    }
                }


                // 4) 임시 데이터 정리
                routeTempMap.remove(amrId);
                submissionStartMap.remove(amrId);
                lastSubmissionMap.remove(amrId);
                lastMissionMap.remove(amrId);

                return;
            }




        } catch (Exception ex) {
            log.error("AMR_STATE 처리 실패", ex);
        }
    }


    private void handleTrafficRequest(Map<String, Object> json, WebSocketSession session) {
        Map<String, Object> body = (Map<String, Object>) json.get("body");
        String amrId = (String) body.get("amrId");
        int nodeId = (Integer) body.get("nodeId");
        int submissionId = (Integer) body.get("submissionId");
        String missionId = (String) body.get("missionId");

        // 이 시점에 세션을 맵에 담아두면, 이후 sendTrafficPermit에서 항상 꺼낼 수 있습니다.
        amrSessions.put(amrId, session);
        //log.info("▶ TRAFFIC_REQ: amrId={} sessionId={} 등록", amrId, session.getId());


        // 기존 로직 그대로
        //log.info("🚥 TRAFFIC_REQ 수신: {} → 노드 {}", amrId, nodeId);
        nodeQueues.computeIfAbsent(nodeId, k -> new ConcurrentLinkedQueue<>());

        String currentOccupant = nodeOccupants.get(nodeId);
        if (currentOccupant == null) {
            nodeOccupants.put(nodeId, amrId);
            sendTrafficPermit(amrId, missionId, submissionId, nodeId, session);
        } else {
            nodeQueues.get(nodeId).add(amrId);
            //log.info("대기열 추가됨: {} → 노드 {}", amrId, nodeId);
        }
    }

    private void sendTrafficPermit(String amrId, String missionId, int submissionId, int nodeId, WebSocketSession session) {
        try {

            //log.info("▶ sendTrafficPermit 호출: amrId={}, session={}, open={}", amrId, session, session != null && session.isOpen());

            Map<String, Object> wrapper = new HashMap<>();

            // 2) header 생성
            Map<String, Object> header = new HashMap<>();
            header.put("msgName", "TRAFFIC_PERMIT");
            header.put("amrId", amrId);
            header.put("time", LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
            wrapper.put("header", header);

            // 3) body 생성 (기존 traffic 맵 내용)
            Map<String, Object> body = new HashMap<>();
            body.put("missionId", missionId);
            body.put("submissionId", submissionId);
            body.put("nodeId", nodeId);
            wrapper.put("body", body);

            // 4) JSON 변환 & 전송
            String message = objectMapper.writeValueAsString(wrapper);


            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(message));
                //log.info("✅ Traffic Permit 전송 성공: {}", message);
            } else {
                log.error("❌ Traffic Permit 전송 실패: {} 세션이 없음", amrId);
            }
        } catch (Exception e) {
            log.error("❌ Traffic Permit 전송 실패", e);
        }
    }

}
