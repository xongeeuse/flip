package com.ssafy.flip.domain.log.repository.route;

import com.ssafy.flip.domain.log.entity.Route;

import java.util.Optional;

public interface RouteRepository {

    Route save(Route route);

    Optional<Route> findById(Long id);

}
