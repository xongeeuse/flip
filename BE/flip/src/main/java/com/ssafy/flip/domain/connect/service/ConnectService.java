package com.ssafy.flip.domain.connect.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface ConnectService {

    void sendMission(String amrId, Map<String, Object> mission) throws IOException;

    void sendTraffic(String amrId, Map<String, Object> traffic) throws IOException;

    void sendMissionCancel(String amrId, Map<String, Object> missionCancel) throws IOException;

    Set<String> getConnectedAmrs();

}
