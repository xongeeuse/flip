package com.ssafy.flip.domain.status.repository;

import com.ssafy.flip.domain.status.entity.HumanStatusRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class HumanStatusRedisManualRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public HumanStatusRedis findAllHumanStatus() {

        String key = "HUMAN_STATUS:HUMAN001";
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);

        return convertMapToHumanStatusRedis(map);
    }

    private HumanStatusRedis convertMapToHumanStatusRedis(Map<Object, Object> map) {
        return HumanStatusRedis.builder()
                .humanId((String) map.get("humanId"))
                .x(Float.parseFloat((String) map.get("x")))
                .y(Float.parseFloat((String) map.get("y")))
                .direction(Float.parseFloat((String) map.get("direction")))
                .state(Integer.parseInt((String) map.get("state")))
                .build();
    }
}
