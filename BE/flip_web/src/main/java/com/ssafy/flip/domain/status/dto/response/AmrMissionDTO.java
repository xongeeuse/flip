package com.ssafy.flip.domain.status.dto.response;

import java.time.LocalDateTime;

public record AmrMissionDTO(
        String amrId,
        String type,
        float startX,
        float startY,
        float targetX,
        float targetY,
        int expectedArrival,
        LocalDateTime startedAt
) {
}
