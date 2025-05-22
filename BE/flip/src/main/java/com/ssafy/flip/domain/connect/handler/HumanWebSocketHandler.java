package com.ssafy.flip.domain.connect.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.connect.dto.request.HumanSaveRequestDTO;
import com.ssafy.flip.domain.connect.dto.request.HumanStartRequestDTO;
import com.ssafy.flip.domain.connect.service.AlgorithmResultConsumer;
import com.ssafy.flip.domain.connect.service.AlgorithmTriggerProducer;
import com.ssafy.flip.domain.connect.service.HumanWebSocketService;
import com.ssafy.flip.domain.connect.service.WebSocketService;
import com.ssafy.flip.domain.line.service.LineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class HumanWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ThreadPoolTaskExecutor humanTaskExecutor;

    private final HumanWebSocketService humanWebSocketService;

    private final LineService lineService;
    private final WebSocketService webSocketService;

    private WebSocketSession session;

    private final AlgorithmTriggerProducer trigger;

    private final AlgorithmResultConsumer algorithmResultConsumer;

    private final StringRedisTemplate stringRedisTemplate;

    private String lastMatchedKey = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("HUMAN 연결 : {}", session.getId());
        this.session = session;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        humanTaskExecutor.execute(() -> {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(payload, Map.class);
                String msgName = (String) ((Map<?, ?>) jsonMap.get("header")).get("msgName");
                switch (msgName) {
                    case "HUMAN_STATE":
                        handleHumanState(jsonMap, session);
                        break;
                    default:
                        log.warn("Unknown message: {}", msgName);
                }
            } catch (Exception ex) {
                log.error("비동기 메시지 처리 중 에러", ex);
            }
        });
    }

    private void handleHumanState(Map<String, Object> json, WebSocketSession session) {
        try {
            HumanSaveRequestDTO requestDTO = objectMapper.convertValue(json, HumanSaveRequestDTO.class);

            humanWebSocketService.saveHuman(requestDTO);

            List<String> cancelledAmrs = new ArrayList<>();

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("EDGE CUT", null);
            String matchedKey = null;

            int edgeId = 0;
            int node1 = 0;
            int node2 = 0;
            if(Math.abs(requestDTO.body().worldX() - 80.5F) <= 1) {
                matchedKey = "E";
            }
            else if(Math.abs(requestDTO.body().worldX() - 70.5F) <= 1) {
                edgeId = 418;
                node1 = 130;
                node2 = 206;
                matchedKey = "A";
            } else if(Math.abs(requestDTO.body().worldX() - 73.5F) <= 1) {
                edgeId = 419;
                node1 = 131;
                node2 = 207;
                matchedKey = "B";
            } else if(Math.abs(requestDTO.body().worldX() - 76.5F) <= 1) {
                edgeId = 420;
                node1 = 132;
                node2 = 208;
                matchedKey = "C";
            } else if(requestDTO.body().worldX() - 69.5F < 0){
                if (lastMatchedKey.equals("D")) {
                return;
                }
                lastMatchedKey = "D";
                payloadMap.put("cancelledAmrs", new ArrayList<>());
                payloadMap.put("cutEdge", edgeId);
                String payload = objectMapper.writeValueAsString(payloadMap);
                trigger.run(payload);
                return;
            } else {
                return;
            }

            if (matchedKey != null && matchedKey.equals(lastMatchedKey)) {
                return;
            }

            for (int i = 1; i <= 20; i++) {
                String amrCancelId = String.format("AMR%03d", i);
                String amrCancelKey = "AMR_STATUS:" + amrCancelId;

                Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(amrCancelKey);

                boolean shouldCancel = false;  // 기본은 취소 X

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

                for(int j = submissionId; j < parsedList.size(); j++){
                    Map<String, Object> parsed = parsedList.get(j);
                    if((Integer) parsed.get("submissionNode") == node1 || (Integer) parsed.get("submissionNode") == node2){
                        shouldCancel = true;
                        break;
                    }
                }

                // ✅ 최종 취소
                if (shouldCancel) {
                    webSocketService.sendCancelMission(amrCancelId);
                    cancelledAmrs.add(amrCancelId);
                }
            }

            lastMatchedKey = matchedKey;

            payloadMap.put("cancelledAmrs", cancelledAmrs);
            payloadMap.put("cutEdge", edgeId);
            String payload = objectMapper.writeValueAsString(payloadMap);
            System.out.println("Edge cut: "+edgeId);
            trigger.run(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendStart() throws IOException {
        HumanStartRequestDTO requestDTO = new HumanStartRequestDTO(new HumanStartRequestDTO.Header("MOVE_TRIGGER", null), new HumanStartRequestDTO.Body());
        String payload = objectMapper.writeValueAsString(requestDTO);
        this.session.sendMessage(new TextMessage(payload));
        // 20초 후에 비동기 작업 실행
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(35000); // 35초 대기
                lineService.repairLine(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }
}
