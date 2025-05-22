package com.ssafy.flip.domain.log.repository.route;

import com.ssafy.flip.domain.log.entity.ErrorLog;
import com.ssafy.flip.domain.log.entity.MissionLog;
import com.ssafy.flip.domain.log.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteJpaRepository extends JpaRepository<Route, Long> {

    Optional<Route> findById(Long id);

}
