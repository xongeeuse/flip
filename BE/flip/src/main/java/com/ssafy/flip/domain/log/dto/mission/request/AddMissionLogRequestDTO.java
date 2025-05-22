package com.ssafy.flip.domain.log.dto.mission.request;

import java.time.LocalDateTime;

public record AddMissionLogRequestDTO(
        String missionId,
        String amrId,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
