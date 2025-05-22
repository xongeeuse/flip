package com.ssafy.flip.domain.status.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.status.dto.response.RouteDTO;
import com.ssafy.flip.domain.status.dto.response.SubmissionDTO;
import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AmrStatusRedisManualRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<AmrStatusRedis> findAllAmrStatus() {
        Set<String> keys = redisTemplate.keys("AMR_STATUS:*");
        if (keys.isEmpty()) return Collections.emptyList();

        return keys.stream()
                .map(key -> {
                    Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                    String amrId = key.replace("AMR_STATUS:", "");
                    return convertMapToAmrStatusRedis(map, amrId);
                })
                .collect(Collectors.toList());
    }

    public Optional<AmrStatusRedis> findByAmrId(String amrId) {
        String key = "AMR_STATUS:" + amrId;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) return Optional.empty();

        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) return Optional.empty();

        return Optional.of(convertMapToAmrStatusRedis(map, amrId));
    }


    private AmrStatusRedis convertMapToAmrStatusRedis(Map<Object, Object> map, String amrId) {
        List<SubmissionDTO> submissionList = parseSubmissionList(map.get("submissionList"));

        List<RouteDTO> routeList = parseJsonStringList(
                castToListOfString(map.get("routeList")), RouteDTO.class);

        return AmrStatusRedis.builder()
                .amrId(amrId)
                .x(Float.parseFloat((String) map.get("x")))
                .y(Float.parseFloat((String) map.get("y")))
                .direction(Float.parseFloat((String) map.getOrDefault("direction", "0")))
                .state(Integer.parseInt((String) map.get("state")))
                .battery(Integer.parseInt((String) map.get("battery")))
                .loading(Boolean.parseBoolean((String) map.get("loading")))
                .linearVelocity(Float.parseFloat((String) map.get("linearVelocity")))
                .currentNode(Integer.parseInt((String) map.get("currentNode")))
                .missionId((String) map.get("missionId"))
                .missionType((String) map.get("missionType"))
                .submissionId(Integer.parseInt((String) map.get("submissionId")))
                .errorList((String) map.get("errorList"))
                .type((String) map.get("type"))
                .submissionList(submissionList)  // 추후 파싱 필요 시 처리
                .routeList(routeList)       // 추후 파싱 필요 시 처리
                .build();
    }

    private List<String> castToListOfString(Object obj) {
        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else if (obj instanceof String str) {
            if (str.isBlank()) {
                return List.of();  // 빈 문자열이면 빈 리스트 반환
            }

            try {
                // 1단계: JSON 문자열 배열로 파싱
                return objectMapper.readValue(str, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("castToListOfString: JSON 문자열 배열 파싱 실패: " + str, e);
            }
        }

        throw new IllegalArgumentException("castToListOfString: 지원하지 않는 타입: " + obj.getClass());
    }

    private List<SubmissionDTO> parseSubmissionList(Object raw) {
        try {
            if (raw == null) return null;
            // raw는 Redis에서 꺼낸 그대로: String
            if (raw instanceof String rawStr) {
                // 1단계: 이중 직렬화 제거
                List<String> innerJsonList = objectMapper.readValue(rawStr, new TypeReference<List<String>>() {});

                // 2단계: 각 문자열을 다시 SubmissionDTO로 파싱
                return innerJsonList.stream()
                        .map(s -> {
                            try {
                                return objectMapper.readValue(s, SubmissionDTO.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("SubmissionDTO 파싱 실패: " + s, e);
                            }
                        })
                        .collect(Collectors.toList());
            }
            throw new IllegalArgumentException("submissionList는 String 타입이어야 합니다.");
        } catch (Exception e) {
            throw new RuntimeException("submissionList 파싱 중 에러", e);
        }
    }

    private <T> List<T> parseJsonStringList(List<String> jsonList, Class<T> clazz) {
        return jsonList.stream()
                .map(json -> {
                    try {
                        // 2단계: 이중으로 인코딩된 JSON 문자열을 다시 디코딩
                        return objectMapper.readValue(json, clazz);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("JSON 파싱 실패: " + json, e);
                    }
                })
                .collect(Collectors.toList());
    }
}
