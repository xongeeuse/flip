package com.ssafy.flip.domain.log.service.mission;

import com.ssafy.flip.domain.amr.service.AmrService;
import com.ssafy.flip.domain.connect.dto.request.RouteTempDTO;
import com.ssafy.flip.domain.log.dto.mission.request.*;
import com.ssafy.flip.domain.log.entity.MissionLog;
import com.ssafy.flip.domain.log.entity.Route;
import com.ssafy.flip.domain.log.repository.mission.MissionLogRepository;
import com.ssafy.flip.domain.log.repository.route.RouteRepository;
import com.ssafy.flip.domain.mission.service.MissionService;
import com.ssafy.flip.domain.node.repository.edge.EdgeRepository;
import com.ssafy.flip.domain.node.repository.node.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionLogServiceImpl implements MissionLogService {


    private final MissionLogRepository missionLogRepository;
    private final MissionService missionService;
    private final AmrService amrService;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final RouteRepository routeRepository;

    @Override
    public MissionLog save(MissionLog missionLog) {
        return missionLogRepository.save(missionLog);
    }

    @Override
    public Optional<MissionLog> findById(Long id) {
        return missionLogRepository.findById(id);
    }

    @Override
    public MissionLog getMissionLog(Long id) {
        return missionLogRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public MissionLog addMissionLog(AddMissionLogRequestDTO requestDTO) {
        MissionLog missionLog = MissionLog.builder()
                .mission(missionService.getMission(requestDTO.missionId()))
                .amr(amrService.getById(requestDTO.amrId()))
                .startedAt(requestDTO.startedAt())
                .endedAt(requestDTO.endedAt())
                .build();

        return missionLogRepository.save(missionLog);
    }

    @Override
    @Transactional
    public MissionLog saveWithRoutes(String amrId, String missionId, List<RouteTempDTO> routes) {
        if (routes == null || routes.isEmpty()) {
            throw new IllegalArgumentException("Route list must not be empty");
        }

        // 첫/마지막 경로로 MissionLog의 시작·종료 시간 설정
        RouteTempDTO first = routes.get(0);
        RouteTempDTO last  = routes.get(routes.size() - 1);

        // 1) MissionLog 저장
        MissionLog log = missionLogRepository.save(
                MissionLog.builder()
                        .mission(missionService.getMission(missionId))
                        .amr(amrService.getById(amrId))
                        .startedAt(first.getStartedAt())
                        .endedAt(last.getEndedAt())
                        .build()
        );

        // 2) 각 RouteTempDTO → Route 엔티티 변환 후 저장
        for (RouteTempDTO r : routes) {
            routeRepository.save(
                    Route.builder()
                            .missionLog(log)
                            // DTO는 getNodeId()/getEdgeId()로 접근
                            .nodeId(nodeRepository.getReferenceById(r.getNodeId()))
                            .edgeId(edgeRepository.findById(r.getEdgeId())
                                    .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 edge_id: " + r.getEdgeId()))
                            )
                            .startedAt(r.getStartedAt())
                            .endedAt(r.getEndedAt())
                            .build()
            );
        }

        return log;
    }
}
