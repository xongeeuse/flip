package com.ssafy.flip.domain.log.service.missionlog;

import com.ssafy.flip.domain.log.entity.MissionLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionLogService {

    Optional<MissionLog> findById(Long id);

    MissionLog getMission(Long id);

    List<MissionLog> findBeforeHour(int hour);

    List<MissionLog> findRecentMissionLogsByMissionIds(List<String> missionIds, LocalDateTime thresholdTime);
}
