package com.ssafy.flip.domain.status.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.line.entity.Line;
import com.ssafy.flip.domain.line.service.LineService;
import com.ssafy.flip.domain.log.entity.MissionLog;
import com.ssafy.flip.domain.log.service.edge.RouteService;
import com.ssafy.flip.domain.log.service.missionlog.MissionLogService;
import com.ssafy.flip.domain.node.entity.Edge;
import com.ssafy.flip.domain.node.entity.Node;
import com.ssafy.flip.domain.node.service.edge.EdgeService;
import com.ssafy.flip.domain.node.service.node.NodeService;
import com.ssafy.flip.domain.status.dto.response.*;
import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import com.ssafy.flip.domain.status.repository.AmrStatusRedisManualRepository;
import com.ssafy.flip.domain.storage.entity.Storage;
import com.ssafy.flip.domain.storage.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService{

    private final AmrStatusRedisManualRepository amrStatusRedisManualRepository;

    private final StorageService storageService;

    private final LineService lineService;

    private final EdgeService edgeService;

    private final NodeService nodeService;

    private final RouteService routeService;

    private final MissionLogService missionLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Map<Integer, Edge> edgeMap;

    private Map<Integer, Node> nodeMap;

    @PostConstruct
    public void initMaps() {
        this.edgeMap = edgeService.findAll().stream()
                .collect(Collectors.toMap(Edge::getEdgeId, edge -> edge));
        this.nodeMap = nodeService.findAll().stream()
                .collect(Collectors.toMap(Node::getNodeId, node -> node));
    }

    @Override
    public AmrRealTimeResponseDTO getAmrRealTimeStatus() {
        List<AmrStatusRedis> amrStatusIterable = amrStatusRedisManualRepository.findAllAmrStatus();
        List<AmrRealTimeDTO> amrRealTimeDTOList = new ArrayList<>();

        for (AmrStatusRedis amrStatus : amrStatusIterable) {
            amrRealTimeDTOList.add(AmrRealTimeDTO.from(amrStatus));
        }

        return new AmrRealTimeResponseDTO(amrRealTimeDTOList);
    }

    @Override
    public MissionStatusResponseDTO getRouteStatus(String amrId) {
        AmrStatusRedis amrStatusRedis = amrStatusRedisManualRepository.findByAmrId(amrId)
                .orElseThrow();

        return MissionStatusResponseDTO.from(amrStatusRedis);
    }

    @Override
    public LineStatusResponseDTO getLineStatus() {
        return null;
    }

    @Override
    public FactoryStatusResponseDTO getFactoryStatus() {

        List<AmrStatusRedis> amrStatusIterable = amrStatusRedisManualRepository.findAllAmrStatus();

        Map<Integer, Long> statusCounts = StreamSupport.stream(amrStatusIterable.spliterator(), false)
                .collect(Collectors.groupingBy(AmrStatusRedis::getState, Collectors.counting()));
        int amrWorking = statusCounts.getOrDefault(2, 0L).intValue();
        int amrCharging = statusCounts.getOrDefault(3, 0L).intValue();
        int amrWaiting = statusCounts.getOrDefault(1, 0L).intValue();
        int amrError = statusCounts.getOrDefault(0, 0L).intValue();

        int amrMaxNum = (int) statusCounts.values().stream().mapToLong(Long::longValue).sum();

        //DB에서 시간 들고와서 계산
        //Redis에서 현재 데이터 들고와서 계산
        List<MissionLog> missionLogs = missionLogService.findBeforeHour(6);

        int amrWorkTime = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.now().minusHours(6);

        for (MissionLog log : missionLogs) {
            LocalDateTime effectiveStartTime = log.getStartedAt().isBefore(targetTime) ? targetTime : log.getStartedAt();
            LocalDateTime effectiveEndTime = log.getEndedAt().isAfter(now) ? now : log.getEndedAt();
            amrWorkTime += (int) Duration.between(effectiveStartTime, effectiveEndTime).toMinutes();
        }

        for (AmrStatusRedis amr : amrStatusIterable) {
            if(amr.getMissionType().equals("MOVING"))
                continue;

            List<RouteDTO> routeList = amr.getRouteList();

            if (routeList != null && !routeList.isEmpty()) {
                try {
                    RouteDTO firstRoute = routeList.getFirst();
                    String startedAtStr = firstRoute.getStartedAt();

                    if (startedAtStr == null || startedAtStr.isBlank()) {
                        System.err.println("[경고] startedAt 값이 null이거나 빈 문자열입니다. amrId: " + amr.getAmrId());
                        continue;
                    }

                    LocalDateTime startedAt = LocalDateTime.parse(startedAtStr, formatter);

                    LocalDateTime effectiveStartTime = startedAt.isBefore(targetTime) ? targetTime : startedAt;

                    amrWorkTime += (int) Duration.between(effectiveStartTime, now).toMinutes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(amrMaxNum != 0) {
            amrWorkTime /= amrMaxNum;
        } else {
            amrWorkTime = 0;
        }

        //DB
        List<Line> lines = lineService.findAll();
        int lineMaxNum = lines.size();
        int lineWorking = (int) lines.stream().filter((line) -> line.isStatus()).count();

        //DB
        Storage storage = storageService.getStorage(1L);
        int storageQuantity = storage.getAmount();
        int storageMaxQuantity = storage.getMaxAmount();

        return new FactoryStatusResponseDTO(
                amrMaxNum,
                amrWorking,
                amrWaiting,
                amrCharging,
                amrError,
                amrWorkTime,
                lineMaxNum,
                lineWorking,
                storageQuantity,
                storageMaxQuantity,
                LocalDateTime.now());
    }

    @Override
    public ProductionResponseDTO getProductionStatus() {

        List<String> productMissions = new ArrayList<>();
        for (int i = 51; i <= 60; i++) {
            productMissions.add("MISSION0" + i);
        }

        LocalDateTime roundedNow = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0); // 정시로 절삭
        LocalDateTime thresholdTime = roundedNow.minusHours(6);

        List<MissionLog> missionLogs = missionLogService.findRecentMissionLogsByMissionIds(
                productMissions, thresholdTime
        );

        Map<Integer, Long> countByHour = groupMissionLogsByHour(missionLogs);

        List<ProductionResponseDTO.ProductionDataDTO> data = new ArrayList<>();
        for (LocalDateTime time = thresholdTime.plusHours(1);
             !time.isAfter(roundedNow);
             time = time.plusHours(1)) {
            int hour = time.getHour();
            long productionCount = countByHour.getOrDefault(hour, 0L);  // 해당 시간대에 로그가 없으면 0
            data.add(new ProductionResponseDTO.ProductionDataDTO(
                    hour,                            // timestamp (시간)
                    (int) productionCount*10,           // production (해당 시간대의 MissionLog 개수)
                    1500                               // target 고정값
            ));
        }

        return new ProductionResponseDTO(data);
    }

    @Override
    public HeatMapResponseDTO getHeatMapStatus() {

        //route 쭉 들고와서
        List<Object[]> routeList = routeService.countRoutesByEdgeIdAfter(LocalDateTime.now().minusHours(6));

        //Edge에 카운트 세고 x, y 찾아서 리턴
        Map<Integer, Integer> nodeCountMap = new HashMap<>();

        List<HeatMapResponseDTO.HeatMapDTO> heatMapList = new ArrayList<>();

        for (Object[] row : routeList) {
            Integer edgeId = (Integer) row[0];
            Long count = (Long) row[1];

            Edge edge = edgeMap.get(edgeId);
            if (edge == null) continue;

            heatMapList.add(new HeatMapResponseDTO.HeatMapDTO(
                    nodeMap.get(edge.getNode1().getNodeId()).getX(),
                    nodeMap.get(edge.getNode1().getNodeId()).getY(),
                    nodeMap.get(edge.getNode2().getNodeId()).getX(),
                    nodeMap.get(edge.getNode2().getNodeId()).getY(),
                    count
            ));
        }

        return new HeatMapResponseDTO(heatMapList);
    }

    private Map<Integer, Long> groupMissionLogsByHour(List<MissionLog> missionLogs) {
        return missionLogs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getStartedAt().getHour(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
}
