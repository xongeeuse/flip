package com.ssafy.flip.domain.connect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.amr.entity.AMR;
import com.ssafy.flip.domain.amr.repository.AmrJpaRepository;
import com.ssafy.flip.domain.connect.dto.request.AmrMissionDTO;
import com.ssafy.flip.domain.mission.dto.MissionResponse;
import com.ssafy.flip.domain.node.entity.Node;
import com.ssafy.flip.domain.node.repository.node.NodeJpaRepository;
import com.ssafy.flip.domain.status.service.StatusService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.ssafy.flip.domain.connect.dto.request.MissionResultWrapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmResultConsumer {

    private final ObjectMapper mapper;
    private final WebSocketService ws;
    private final StringRedisTemplate redis;
    private final WebTriggerProducer webTriggerProducer;
    private final AmrJpaRepository amrJpaRepository;
    private final NodeJpaRepository nodeJpaRepository;
    private final StatusService statusService;



    // ✅ 지연 미션 저장용 해시맵
    @Getter
    private final Map<String, MissionResponse> delayedMissionMap = new ConcurrentHashMap<>();

    private final Map<String, AmrMissionDTO> amrMissionList = new HashMap<>();

    @KafkaListener(topics = "algorithm-result", groupId = "flip-algorithm-group", concurrency = "4")
    public void applyResult(String msg) {


        try {
            MissionResultWrapper wrapper = mapper.readValue(
                    msg, MissionResultWrapper.class
            );

            String triggeredAmr = wrapper.getTriggeredAmr();
            List<MissionResponse> responses = wrapper.getMissions();

            log.info("✅ Kafka 결과 수신: triggeredAmr={}, 총 {}개", triggeredAmr, responses.size());

            // ✅ 미션 취소 (trigger된 AMR이 있는 경우)
//            if (triggeredAmr != null) {
//                for (MissionResponse res : responses) {
//                    if (!triggeredAmr.equals(res.getAmrId())) {
//                        sendCancelMission(res.getAmrId());
//                    }
//                }
//            }

            for (MissionResponse res : responses) {
                if (res.getMissionType().equals("CHARGING")){
                    processMission(res);
                    getDelayedMissionMap().remove(res.getAmrId());
                }
                else{
                    List<MissionResponse> split = splitRoute(res);
                    MissionResponse now = split.get(0);

                    if (split.get(0).getRoute().size() == 1 && split.size() > 1){
                        MissionResponse delayed = split.get(1);
                        processMission(delayed);
                    }
                    else {

                        // 첫 미션 즉시 처리
                        processMission(now);

                        // 두 번째 미션은 해시맵에 저장
                        if (split.size() > 1) {
                            MissionResponse delayed = split.get(1);
                            getDelayedMissionMap().remove(res.getAmrId());
                            delayedMissionMap.put(delayed.getAmrId(), delayed);
                            log.info("✅ Kafka 2번째 미션 저장함: AMRID={}, 값은 {}", delayed.getAmrId(), delayed);
                            String key = "AMR_STATUS:" + delayed.getAmrId();
                            redis.opsForHash().put(key, "finalGoal", String.valueOf(delayed.getRoute().getLast()));
                        } else {
                            log.info("❗ 비상 2번 째 미션이 없는데 실행됨");
                        }
                    }
                }
            }

            sendWebTrigger();

        } catch (Exception e) {
            log.error("❗ 알고리즘 결과 처리 실패", e);
        }

    }



    // ✅ 미션 즉시 실행 로직 (WebSocket 전송 포함)
    public void processMission(MissionResponse res) throws JsonProcessingException {
        String amrId = res.getAmrId();
        String key = "AMR_STATUS:" + amrId;
        redis.opsForHash().put(key, "missionId", String.valueOf(res.getMissionId()));
        redis.opsForHash().put(key, "missionType", String.valueOf(res.getMissionType()));

        ws.sendMission(amrId, res);

        String type = amrJpaRepository.findById(amrId)
                .map(AMR::getType)
                .orElse("UNKNOWN");

        List<Integer> routes = res.getRoute();
        int startNodeId = routes.getFirst();
        int targetNodeId = routes.getLast();

        Node startNode = nodeJpaRepository.findById(startNodeId)
                .orElseThrow(() -> new RuntimeException("노드 정보 없음: " + startNodeId));
        Node targetNode = nodeJpaRepository.findById(targetNodeId)
                .orElseThrow(() -> new RuntimeException("노드 정보 없음: " + targetNodeId));

        List<String> submissionList = IntStream.range(0, routes.size())
                .mapToObj(i -> {
                    int nodeId = routes.get(i);
                    Node node = nodeJpaRepository.findById(nodeId).orElseThrow();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("submissionId", i + 1);
                    map.put("submissionNode", nodeId);
                    map.put("submissionX", node.getX());
                    map.put("submissionY", node.getY());
                    try {
                        return mapper.writeValueAsString(map);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("JSON 직렬화 실패", e);
                    }
                }).toList();

        statusService.updateSubmissionList(amrId, submissionList);

        amrMissionList.put(res.getAmrId(), new AmrMissionDTO(
                res.getAmrId(),
                res.getMissionType(),
                startNode.getX(),
                startNode.getY(),
                targetNode.getX(),
                targetNode.getY(),
                res.getExpectedArrival(),
                LocalDateTime.now()
        ));
    }

    // ✅ 미션 분할 로직
    private List<MissionResponse> splitRoute(MissionResponse res) {
        List<Integer> route = res.getRoute();
        Set<Integer> splitPoints = new HashSet<>();
        IntStream.rangeClosed(1, 10).forEach(splitPoints::add);
        IntStream.rangeClosed(21, 30).forEach(splitPoints::add);
        IntStream.rangeClosed(41, 50).forEach(splitPoints::add);

        for (int i = route.size() - 2; i >= 0; i--) {
            int node = route.get(i);
            if (splitPoints.contains(node)) {
                List<Integer> r1 = new ArrayList<>(route.subList(0, i + 1));
                List<Integer> r2 = new ArrayList<>(route.subList(i, route.size()));

                MissionResponse part1 = new MissionResponse(
                        res.getAmrId(),
                        String.format("MISSION%03d", r1.get(r1.size() - 1)),
                        "LOADING",
                        res.getExpectedArrival(),
                        r1
                );

                MissionResponse part2 = new MissionResponse(
                        res.getAmrId(),
                        String.format("MISSION%03d", r2.get(r2.size() - 1)),
                        "UNLOADING",
                        res.getExpectedArrival(),
                        r2
                );


                return List.of(part1, part2);
            }
        }

        return List.of(res); // 분리 불가 → 단일 미션 유지
    }

    public void sendWebTrigger() throws JsonProcessingException {
        String payload = mapper.writeValueAsString(amrMissionList.values().stream().toList());
        //log.info("✅ Web Trigger 전송: {}", payload);
        webTriggerProducer.run(payload);
    }
}
