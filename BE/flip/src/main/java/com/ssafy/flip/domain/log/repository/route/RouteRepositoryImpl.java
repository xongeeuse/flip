package com.ssafy.flip.domain.log.repository.route;

import com.ssafy.flip.domain.log.entity.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RouteRepositoryImpl implements RouteRepository {

    private final RouteJpaRepository routeJpaRepository;

    @Override
    public Route save(Route route) {
        return routeJpaRepository.save(route);
    }

    @Override
    public Optional<Route> findById(Long id) {
        return routeJpaRepository.findById(id);
    }
}
