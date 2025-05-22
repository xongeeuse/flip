package com.ssafy.flip.domain.log.repository.edge;

import com.ssafy.flip.domain.log.entity.MissionLog;
import com.ssafy.flip.domain.log.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteJpaRepository extends JpaRepository<Route, Long> {

    List<Route> findAll();

    Optional<Route> findById(Long id);

    List<Route> findByEndedAtAfter(LocalDateTime time);

    @Query("SELECT r.edgeId.id, COUNT(r) FROM Route r WHERE r.endedAt >= :time GROUP BY r.edgeId.id")
    List<Object[]> countRoutesByEdgeIdAfter(@Param("time") LocalDateTime time);
}
