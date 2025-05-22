package com.ssafy.flip.domain.log.service.edge;

import com.ssafy.flip.domain.log.entity.Route;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteService {

    List<Route> findAll();

    Optional<Route> findById(Long id);

    Route getEdge(Long id);

    List<Route> findByEndedAtAfter(LocalDateTime time);

    List<Object[]> countRoutesByEdgeIdAfter(LocalDateTime time);
}
