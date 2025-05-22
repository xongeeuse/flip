package com.ssafy.flip.domain.mission.repository;

import com.ssafy.flip.domain.mission.entity.Mission;

import java.util.Optional;

public interface MissionRepository {

    Optional<Mission> findById(String missionId);

}
