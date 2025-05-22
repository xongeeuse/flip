package com.ssafy.flip.domain.status.repository;

import com.ssafy.flip.domain.status.entity.LineStatusRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LineStatusRedisManualRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public List<LineStatusRedis> findAllLineStatus() {
        Set<String> keys = redisTemplate.keys("LINE_STATUS:*");
        if (keys.isEmpty()) return Collections.emptyList();

        return keys.stream()
                .map(key -> {
                    Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                    String lineIdStr = key.replace("LINE_STATUS:", ""); // e.g., "LINE001"
                    // "LINE001" -> "001" -> 1
                    String numericPart = lineIdStr.replaceAll("\\D+", ""); // 정수만 추출
                    Long lineId = Long.parseLong(numericPart);
                    return convertMapToLineStatusRedis(map, lineId);
                })
                .collect(Collectors.toList());
    }

    public Optional<LineStatusRedis> findByLineId(Long lineId) {
        String key = "LINE_STATUS:" + lineId;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) return Optional.empty();

        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) return Optional.empty();

        return Optional.of(convertMapToLineStatusRedis(map, lineId));
    }

    private LineStatusRedis convertMapToLineStatusRedis(Map<Object, Object> map, Long lineId) {
        return LineStatusRedis.builder()
                .lineId(lineId)
                .cycleTime(Float.parseFloat((String) map.get("cycleTime")))
                .status(Boolean.parseBoolean((String) map.get("status")))
                .lastInputTime(LocalDateTime.parse((CharSequence) map.get("lastInputTime")))
                .build();
    }
}
