package com.ssafy.flip.domain.log.dto.route.request;

import java.time.LocalDateTime;

public record AddRouteRequestDTO(
        Long routeId,
        Long missionLogId,
        Integer nodeId,
        Integer edgeId,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
