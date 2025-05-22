package com.ssafy.flip.domain.status.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.amr.service.AmrService;
import com.ssafy.flip.domain.connect.service.AlgorithmTriggerProducer;
import com.ssafy.flip.domain.line.entity.Line;
import com.ssafy.flip.domain.line.service.LineService;
import com.ssafy.flip.domain.status.dto.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final AmrService amrService;

    private final LineService lineService;

    private final AlgorithmTriggerProducer algorithmTriggerProducer;

    @Override
    public void saveAmr(AmrSaveRequestDTO amrDto, List<String> routeList) {
        String key = "AMR_STATUS:" + amrDto.body().amrId();
        Map<String, String> map = new HashMap<>();
        map.put("x", String.valueOf(amrDto.body().worldX()));
        map.put("y", String.valueOf(amrDto.body().worldY()));
        map.put("direction", String.valueOf(amrDto.body().dir()));
        map.put("state", String.valueOf(amrDto.body().state()));
        map.put("battery", String.valueOf(amrDto.body().battery()));
        map.put("loading", String.valueOf(amrDto.body().loading()));
        map.put("linearVelocity", String.valueOf(amrDto.body().linearVelocity()));
        map.put("currentNode", String.valueOf(amrDto.body().currentNode()));
        map.put("missionId", String.valueOf(amrDto.body().missionId()));
        map.put("missionType", amrDto.body().missionType() != null ? amrDto.body().missionType() : "UNKNOWN");
        map.put("errorList", String.valueOf(amrDto.body().errorList()));
        map.put("submissionId", String.valueOf(amrDto.body().submissionId()));

        String type = amrService.getById(amrDto.body().amrId()).getType();
        map.put("type", type);

        try {
            String route = objectMapper.writeValueAsString(routeList);
            map.put("routeList", route);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        stringRedisTemplate.opsForHash().putAll(key, map);
}

    @Override
    public void updateSubmissionList(String amrId, List<String> submissoinList) {
        String key = "AMR_STATUS:" + amrId;
        try {
            String jsonString = objectMapper.writeValueAsString(submissoinList);
            stringRedisTemplate.opsForHash().put(key, "submissionList", jsonString);
        } catch (JsonProcessingException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void updateRouteList(String amrId, List<String> routeList) {
        String key = "AMR_STATUS:" + amrId;
        try {
            String jsonString = new ObjectMapper().writeValueAsString(routeList);
            stringRedisTemplate.opsForHash().put(key, "routeList", jsonString);
        } catch (JsonProcessingException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    public void saveLine(Integer lineId) {
        String key = "LINE_STATUS:" + String.format("LINE%03d", lineId);

        Line line = lineService.getLine(lineId.longValue());

        Map<String, String> map = new HashMap<>();
        map.put("cycleTime", String.valueOf(line.getCycleTime()));
        map.put("status", String.valueOf(line.isStatus()));
        map.put("lastInputTime", String.valueOf(LocalDateTime.now()));

        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void repairLine() {
        lineService.repairLine(10L);
    }

    @Override
    public void brokeLine() {
        lineService.brokeLine(10L);
        String kafkaPayload = "LINE BROKEN : "+0;
        algorithmTriggerProducer.run(kafkaPayload);

        String key = "LINE_STATUS:" + String.format("LINE%03d", 10);
        Map<String, String> map = new HashMap<>();
        map.put("status", String.valueOf(false));
        stringRedisTemplate.opsForHash().putAll(key, map);
    }
}
