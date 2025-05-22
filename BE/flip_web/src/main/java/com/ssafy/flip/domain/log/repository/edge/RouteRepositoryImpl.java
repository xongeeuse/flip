package com.ssafy.flip.domain.log.repository.edge;

import com.ssafy.flip.domain.log.entity.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RouteRepositoryImpl implements RouteRepository {

    private final RouteJpaRepository edgeJpaRepository;

    @Override
    public List<Route> findAll() {
        return edgeJpaRepository.findAll();
    }

    @Override
    public Optional<Route> findById(Long id) {
        return edgeJpaRepository.findById(id);
    }

    @Override
    public List<Route> findByEndedAtAfter(LocalDateTime time) {
        return edgeJpaRepository.findByEndedAtAfter(time);
    }

    @Override
    public List<Object[]> countRoutesByEdgeIdAfter(LocalDateTime time){
        return edgeJpaRepository.countRoutesByEdgeIdAfter(time);
    }

}
