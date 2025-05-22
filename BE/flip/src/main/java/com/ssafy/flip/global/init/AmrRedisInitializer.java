package com.ssafy.flip.global.init;

import com.ssafy.flip.domain.amr.entity.AMR;
import com.ssafy.flip.domain.amr.service.AmrService;
import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class AmrRedisInitializer {

    private final AmrService amrService;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void initRedisForAmrs() {
        List<String> amrIds = amrService.findAll()
                .stream()
                .map(AMR::getAmrId)
                .toList();

        for (String amrId : amrIds) {
            String key = "AMR_STATUS:" + amrId;

            // 무조건 새로 덮어씀
            AmrStatusRedis emptyStatus = AmrStatusRedis.builder()
                    .amrId(amrId)
                    .x(0)
                    .y(0)
                    .direction(0)
                    .state(0)
                    .battery(0)
                    .loading(false)
                    .linearVelocity(0)
                    .currentNode(0)
                    .missionId("")
                    .missionType("UNKNOWN")
                    .submissionId(0)
                    .errorList("")
                    .type("")
                    .submissionList(List.of())
                    .routeList(List.of())
                    .build();

            stringRedisTemplate.opsForHash().putAll(key, toMap(emptyStatus));
        }
    }


    private Map<String, String> toMap(AmrStatusRedis status) {
        Map<String, String> map = new HashMap<>();
        map.put("amrId", status.getAmrId());
        map.put("x", String.valueOf(status.getX()));
        map.put("y", String.valueOf(status.getY()));
        map.put("direction", String.valueOf(status.getDirection()));
        map.put("state", String.valueOf(status.getState()));
        map.put("battery", String.valueOf(status.getBattery()));
        map.put("loading", String.valueOf(status.isLoading()));
        map.put("linearVelocity", String.valueOf(status.getLinearVelocity()));
        map.put("currentNode", String.valueOf(status.getCurrentNode()));
        map.put("missionId", status.getMissionId());
        map.put("missionType", status.getMissionType());
        map.put("submissionId", String.valueOf(status.getSubmissionId()));
        map.put("errorList", status.getErrorList());
        map.put("type", status.getType());
        map.put("submissionList", status.getSubmissionList().toString());
        map.put("routeList", status.getRouteList().toString());
        return map;
    }


    @PostConstruct
    public void initializeMissionPtInRedis() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String now = LocalDateTime.now().format(formatter);

        IntStream.rangeClosed(11, 50).forEach(i -> {
            String key = "MISSION_PT:" + i;
            stringRedisTemplate.opsForValue().set(key, now); // 무조건 덮어씀
        });
    }


}
