package com.ssafy.flip.domain.connect.controller;

import com.ssafy.flip.domain.connect.handler.HumanWebSocketHandler;
import com.ssafy.flip.domain.connect.service.ConnectService;
import com.ssafy.flip.domain.connect.service.HumanWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/api/v1/connect")
@RequiredArgsConstructor
public class ConnectController {

    private final ConnectService connectService;
    private final StringRedisTemplate redisTemplate;
    private final HumanWebSocketHandler humanWebSocketHandler;

    // 연결된 AMR 목록 조회
    @GetMapping("/")
    public ResponseEntity<Set<String>> getConnectedAmrs() {
        return ResponseEntity.ok(connectService.getConnectedAmrs());
    }

    @PostMapping("/mission")
    public ResponseEntity<String> missionAssign(@RequestBody Map<String, Object> mission) {
        try {
            connectService.sendMission((String) mission.get("amrId"), mission);
            return ResponseEntity.ok("미션 전송 완료");
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/traffic")
    public ResponseEntity<String> trafficPermit(@RequestBody Map<String, Object> mission) {
        try {
            connectService.sendTraffic((String) mission.get("amrId"), mission);
            return ResponseEntity.ok("교통 제어 전송 완료");
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> missionCancel(@RequestBody Map<String, Object> missionCancel) {
        try {
            connectService.sendMissionCancel((String) missionCancel.get("amrId"), missionCancel);
            return ResponseEntity.ok("미션 취소 완료");
        } catch (IOException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/algorithm/test")
    public ResponseEntity<String> triggerAlgorithm() {
        redisTemplate.convertAndSend("algorithm-trigger", "RUN");
        return ResponseEntity.ok("📡 Algorithm trigger sent.");
    }

    @PostMapping("/human")
    public ResponseEntity<String> triggerHuman() throws IOException {
        humanWebSocketHandler.sendStart();
        return ResponseEntity.ok("Human start");
    }
}