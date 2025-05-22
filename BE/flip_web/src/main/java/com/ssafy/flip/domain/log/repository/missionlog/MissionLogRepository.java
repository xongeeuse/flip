package com.ssafy.flip.domain.log.repository.missionlog;

import com.ssafy.flip.domain.log.entity.MissionLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionLogRepository {

    List<MissionLog> findAll();

    Optional<MissionLog> findById(Long id);

    List<MissionLog> findByEndedAtBefore(LocalDateTime endTime);

    List<MissionLog> findRecentMissionLogsByMissionIds(List<String> missionIds, LocalDateTime thresholdTime);
}
