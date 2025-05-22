package com.ssafy.flip.domain.connect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.flip.domain.connect.dto.response.EdgeDTO;
import com.ssafy.flip.domain.connect.dto.response.MapInfoDTO;
import com.ssafy.flip.domain.connect.dto.response.MissionAssignDTO;
import com.ssafy.flip.domain.connect.dto.response.NodeDTO;
import com.ssafy.flip.domain.mission.dto.MissionResponse;
import com.ssafy.flip.domain.node.entity.Edge;
import com.ssafy.flip.domain.node.entity.Node;
import com.ssafy.flip.domain.node.service.edge.EdgeService;
import com.ssafy.flip.domain.node.service.node.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final ObjectMapper objectMapper;
    private final NodeService nodeService;
    private final EdgeService edgeService;
    private final AlgorithmTriggerProducer trigger;   // ← Kafka로 트리거
    private final ObjectMapper mapper;

    // 🔧 AmrWebSocketHandler를 제거하고 세션 Map 직접 관리
    private final Map<String, WebSocketSession> amrSessions = new ConcurrentHashMap<>();
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void registerSession(String amrId, WebSocketSession session) {
        amrSessions.put(amrId, session);
    }

    @Override
    public String sendMapInfo() {
        List<Node> nodeList = nodeService.findAll();
        List<Edge> edgeList = edgeService.findAll();

        // DTO 변환
        List<NodeDTO> nodeDTOs = nodeList.stream()
                .map(NodeDTO::from)
                .toList();

        List<EdgeDTO> edgeDTOs = edgeList.stream()
                .map(EdgeDTO::from)
                .toList();

        // MapInfo 생성
        MapInfoDTO mapInfoDTO = MapInfoDTO.of(nodeDTOs, edgeDTOs);

        // JSON 직렬화
        try {
            return objectMapper.writeValueAsString(mapInfoDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("맵 정보 직렬화 실패", e);
        }
    }

    public void sendMission(String amrId, MissionResponse res) {
        try {
            List<Integer> route = res.getRoute();
            List<MissionAssignDTO.SubmissionDTO> submissions = new ArrayList<>();

            String redisKey = "AMR_STATUS:" + amrId;
            Object rawStartId = stringRedisTemplate.opsForHash().get(redisKey, "submissionId");

            int startSubmissionId = 0; // 기본값
            try {
                if (rawStartId != null) {
                    startSubmissionId = Integer.parseInt(rawStartId.toString());
                }
            } catch (NumberFormatException e) {
                log.warn("⚠️ submissionId 파싱 실패: {}", rawStartId);
            }

            for (int i = 1; i < route.size(); i++) {
                int prev = route.get(i - 1);
                int curr = route.get(i);

                int node1 = Math.min(prev, curr);
                int node2 = Math.max(prev, curr);
                String edgeKey = node1 + "-" + node2;

                String edgeId = edgeService.getEdgeKeyToIdMap().getOrDefault(edgeKey, "UNKNOWN");

                if ("UNKNOWN".equals(edgeId)) {
                    log.error("❗ 존재하지 않는 edgeKey: {} AMRID {}", edgeKey, amrId);

                    // 🚨 미션 즉시 취소 후 89번으로 유배 보내는 Kafka 메시지 전송
                    sendCancelMission(amrId);
                    String key = "AMR_STATUS:" + amrId;
                    stringRedisTemplate.opsForHash().put(key, "submissionList", "[]");
                    stringRedisTemplate.opsForHash().put(key, "submissionId", "0");

                    // 5초 후 Kafka 메시지 비동기 전송
                    String kafkaPayload = "None Edge Error : " + amrId;
                    stringRedisTemplate.opsForHash().put(key, "loading", "false");
                    trigger.run(kafkaPayload);  // Kafka 전송
                    log.info("📤 5초 후 Kafka 메시지 전송: {}", kafkaPayload);


                    // 예외 발생 (중단)
                    //throw new IllegalArgumentException("Invalid edgeKey: " + edgeKey);
                }

                int submissionId = startSubmissionId + (i);
                submissions.add(new MissionAssignDTO.SubmissionDTO(
                        //String.valueOf(submissionId),
                        String.valueOf(i),
                        String.valueOf(curr),
                        edgeId));
            }

            MissionAssignDTO missionAssignDTO = MissionAssignDTO.of(
                    amrId,
                    res.getMissionId(),
                    res.getMissionType(),
                    submissions
            );
            
            String payload = objectMapper.writeValueAsString(missionAssignDTO);
            //log.info("payload는 이값입니다 : {}",payload);
            WebSocketSession session = amrSessions.get(amrId);

            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
                log.info("📤 미션 전송 완료: AMR = {}, Payload = {}", amrId, payload);
            } else {
                log.warn("❗ WebSocket 세션 없음: AMR = {}", amrId);
            }

        } catch (Exception e) {
            log.error("❗ sendMission 전송 실패", e);
        }
    }

    @Override
    public Map<String, WebSocketSession> getAmrSessions() {
        return amrSessions;
    }

    @Override
    public void sendCancelMission(String amrId) {
        try {
            //getDelayedMissionMap().remove(amrId);
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("msgName", "MISSION_CANCEL");
            header.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
            header.put("amrId", amrId);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("state", "");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("header", header);
            payload.put("body", body);

            String json = mapper.writeValueAsString(payload);
            WebSocketSession session = getAmrSessions().get(amrId);

            if (session != null && session.isOpen()) {
                synchronized (session) {  // 🛡️ 동시성 제어
                    session.sendMessage(new TextMessage(json));
                }
                log.info("📤 MISSION_CANCEL 전송 완료: AMR = {}, Payload = {}", amrId, json);
            } else {
                log.warn("❗ WebSocket 세션 없음: AMR = {}", amrId);
            }

        } catch (Exception e) {
            log.error("❗ MISSION_CANCEL 전송 실패: AMR = {}", amrId, e);
        }
    }

}
