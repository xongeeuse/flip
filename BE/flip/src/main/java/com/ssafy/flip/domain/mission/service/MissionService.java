package com.ssafy.flip.domain.mission.service;

import com.ssafy.flip.domain.mission.entity.Mission;

import java.util.Optional;

public interface MissionService {

    Optional<Mission> findById(String missionId);

    Mission getMission(String missionId);
}
