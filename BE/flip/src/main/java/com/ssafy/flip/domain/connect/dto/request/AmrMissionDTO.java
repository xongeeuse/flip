package com.ssafy.flip.domain.connect.dto.request;

import java.time.LocalDateTime;
import java.util.List;

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
