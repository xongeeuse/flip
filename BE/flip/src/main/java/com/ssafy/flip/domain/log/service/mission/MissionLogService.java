package com.ssafy.flip.domain.log.service.mission;

import com.ssafy.flip.domain.connect.dto.request.RouteTempDTO;
import com.ssafy.flip.domain.log.dto.mission.request.*;
import com.ssafy.flip.domain.log.entity.MissionLog;

import java.util.List;
import java.util.Optional;

public interface MissionLogService {

    MissionLog save(MissionLog missionLog);

    Optional<MissionLog> findById(Long id);

    MissionLog getMissionLog(Long id);

    MissionLog addMissionLog(AddMissionLogRequestDTO requestDTO);

    MissionLog saveWithRoutes(String amrId, String missionId, List<RouteTempDTO> routes);

}
