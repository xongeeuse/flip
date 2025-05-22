package com.ssafy.flip.domain.log.repository.mission;

import com.ssafy.flip.domain.log.entity.MissionLog;

import java.util.Optional;

public interface MissionLogRepository {

    MissionLog save(MissionLog missionLog);

    Optional<MissionLog> findById(Long id);

}
