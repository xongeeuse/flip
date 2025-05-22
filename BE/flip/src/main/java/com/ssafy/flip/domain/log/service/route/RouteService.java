package com.ssafy.flip.domain.log.service.route;

import com.ssafy.flip.domain.log.dto.route.request.AddRouteRequestDTO;
import com.ssafy.flip.domain.log.entity.Route;

import java.util.Optional;

public interface RouteService {

    Route save(Route route);

    Optional<Route> findById(Long id);

    Route addRoute(AddRouteRequestDTO requestDTO);

}
