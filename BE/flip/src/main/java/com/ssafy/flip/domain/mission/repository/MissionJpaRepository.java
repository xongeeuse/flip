package com.ssafy.flip.domain.mission.repository;

import com.ssafy.flip.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionJpaRepository extends JpaRepository<Mission, String> {

    Optional<Mission> findById(String missionId);

}
