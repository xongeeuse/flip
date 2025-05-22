package com.ssafy.flip.domain.connect.service;

import com.ssafy.flip.domain.mission.dto.MissionResponse;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

public interface WebSocketService {

    String sendMapInfo();

    void sendMission(String amrId, MissionResponse res);

    void registerSession(String amrId, WebSocketSession session);

    Map<String, WebSocketSession> getAmrSessions();

    void sendCancelMission(String amrId);

}
