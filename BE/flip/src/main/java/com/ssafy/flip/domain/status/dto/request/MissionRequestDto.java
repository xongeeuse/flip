package com.ssafy.flip.domain.status.dto.request;

import java.util.List;

public record MissionRequestDto(
        Header header,
        Body body
) {
    public record Header(
            String msgName,
            String time
    ) {}

    public record Body(
            int missionId,
            String missionType,
            int expectedArrival,
            List<Routes> routes
    ) {}

    public record Routes(
            int nodeId
    ) {}
}
