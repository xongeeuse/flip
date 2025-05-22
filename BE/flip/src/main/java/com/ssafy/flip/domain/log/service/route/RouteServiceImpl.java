package com.ssafy.flip.domain.log.service.route;

import com.ssafy.flip.domain.log.dto.route.request.AddRouteRequestDTO;
import com.ssafy.flip.domain.log.entity.Route;
import com.ssafy.flip.domain.log.repository.route.RouteRepository;
import com.ssafy.flip.domain.log.service.mission.MissionLogService;
import com.ssafy.flip.domain.node.service.edge.EdgeService;
import com.ssafy.flip.domain.node.service.node.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    private final MissionLogService missionLogService;

    private final NodeService nodeService;

    private final EdgeService edgeService;

    @Override
    public Route save(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route addRoute(AddRouteRequestDTO requestDTO) {
        Route route = Route.builder()
                .missionLog(missionLogService.getMissionLog(requestDTO.missionLogId()))
                .nodeId(nodeService.getNode(requestDTO.nodeId()))
                .edgeId(edgeService.getEdge(requestDTO.edgeId()))
                .startedAt(requestDTO.startedAt())
                .endedAt(requestDTO.endedAt())
                .build();

        return routeRepository.save(route);
    }
}
