package com.ssafy.flip.domain.connect.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.connect.dto.response.*;
import com.ssafy.flip.domain.connect.handler.AmrWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConnectServiceImpl implements ConnectService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> amrSessions;

    public ConnectServiceImpl(AmrWebSocketHandler amrHandler) {
        this.amrSessions = amrHandler.getAmrSessions();
    }

    public void sendMission(String amrId, Map<String, Object> mission) throws IOException {
        WebSocketSession session = amrSessions.get(amrId);
        if (session != null && session.isOpen()) {
            Object submissionsObj = mission.get("submissions");

            // LinkedHashMap -> DTO 변환 (명시적)
            List<MissionAssignDTO.SubmissionDTO> submissions = objectMapper.convertValue(
                    submissionsObj,
                    new TypeReference<List<MissionAssignDTO.SubmissionDTO>>() {}
            );

            MissionAssignDTO missionAssignDTO = MissionAssignDTO.of(
                    amrId,
                    (String) mission.get("missionId"),
                    (String) mission.get("missionType"),
                    submissions
            );

            String json = objectMapper.writeValueAsString(missionAssignDTO);
            session.sendMessage(new TextMessage(json));
        } else {
            throw new IllegalStateException("해당 AMR이 존재하지 않음 : " + amrId);
        }
    }

    @Override
    public void sendTraffic(String amrId, Map<String, Object> traffic) throws IOException {
        WebSocketSession session = amrSessions.get(amrId);

        if (session == null) {
            throw new IllegalStateException("❌ AMR 세션 없음: " + amrId);
        }

        if (!session.isOpen()) {
            throw new IllegalStateException("❌ 세션이 닫힘: " + amrId);
        }

        // ✅ 필요한 값 추출
        String missionId = (String) traffic.get("missionId");
        Integer submissionId = (Integer) traffic.get("submissionId");
        Integer nodeId = (Integer) traffic.get("nodeId");

        // ✅ DTO 생성 및 직렬화
        TrafficPermitDTO dto = TrafficPermitDTO.of(amrId, missionId, submissionId, nodeId);
        String json = objectMapper.writeValueAsString(dto);

        // ✅ 전송
        session.sendMessage(new TextMessage(json));
        System.out.println("✅ TRAFFIC_PERMIT 전송됨 → AMR: " + amrId + ", 노드: " + nodeId);
    }


    @Override
    public void sendMissionCancel(String amrId, Map<String, Object> missionCancel) throws IOException {
        WebSocketSession session = amrSessions.get(amrId);
        if (session != null && session.isOpen()) {
            MissionCancelDTO missionCancelDTO = MissionCancelDTO.of(
                    amrId,
                    (String) missionCancel.get("state"));
            String json = objectMapper.writeValueAsString(missionCancelDTO);
            session.sendMessage(new TextMessage(json));
        } else {
            throw new IllegalStateException("해당 AMR이 존재하지 않음 : " + amrId);
        }
    }

    public Set<String> getConnectedAmrs() {
        return amrSessions.keySet();
    }

}
