package com.ssafy.flip.domain.connect.service;

import com.ssafy.flip.domain.connect.dto.request.HumanSaveRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HumanWebSocketServiceImpl implements HumanWebSocketService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveHuman(HumanSaveRequestDTO requestDTO) {
        String key = "HUMAN_STATUS:HUMAN001";
        Map<String, String> map = new HashMap<>();
        map.put("humanId", requestDTO.body().humanId());
        map.put("x", String.valueOf(requestDTO.body().worldX()));
        map.put("y", String.valueOf(requestDTO.body().worldY()));
        map.put("direction", String.valueOf(requestDTO.body().dir()));
        map.put("state", String.valueOf(requestDTO.body().state()));

        stringRedisTemplate.opsForHash().putAll(key, map);
    }
}
