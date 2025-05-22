package com.ssafy.flip.domain.connect.dto.response;

public record TrafficPermitDTO(
        Header header,
        Body body
) {
    public static TrafficPermitDTO of(String amrId, String missionId, Integer submissionId, Integer nodeId) {
        return new TrafficPermitDTO(
                new Header("TRAFFIC_PERMIT", java.time.LocalDateTime.now().toString(), amrId),
                new Body(missionId, submissionId, nodeId)
        );
    }

    public record Header(
            String msgName,
            String time,
            String amrId
    ) {}

    public record Body(
            String missionId,
            Integer submissionId,
            Integer nodeId
    ) {}
}