package com.ssafy.flip.domain.log.service.edge;

import com.ssafy.flip.domain.log.entity.Route;
import com.ssafy.flip.domain.log.repository.edge.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Override
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Override
    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route getEdge(Long id) {
        return routeRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<Route> findByEndedAtAfter(LocalDateTime time) {
        return routeRepository.findByEndedAtAfter(time);
    }

    @Override
    public List<Object[]> countRoutesByEdgeIdAfter(LocalDateTime time) {
        return routeRepository.countRoutesByEdgeIdAfter(time);
    }


}
