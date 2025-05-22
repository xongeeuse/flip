package com.ssafy.flip.domain.log.repository.mission;

import com.ssafy.flip.domain.log.entity.ErrorLog;
import com.ssafy.flip.domain.log.entity.MissionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionLogJpaRepository extends JpaRepository<MissionLog, Long> {

    Optional<MissionLog> findById(Long id);

}
