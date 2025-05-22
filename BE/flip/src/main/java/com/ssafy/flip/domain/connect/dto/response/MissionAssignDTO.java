package com.ssafy.flip.domain.connect.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MissionAssignDTO(
        Header header,
        Body body
) {
    public static MissionAssignDTO of(String amrId, String missionId, String missionType, List<SubmissionDTO> submissions) {
        return new MissionAssignDTO(
                new Header("MISSION_ASSIGN", java.time.LocalDateTime.now().toString(), amrId),
                new Body(missionId, missionType, submissions)
        );
    }

    public record Header(
            String msgName,
            String time,
            String amrId
    ) {}

    public record Body(
            String missionId,
            String missionType,
            List<SubmissionDTO> submissions
    ) {}

    public record SubmissionDTO(
            int submissionId,
            int nodeId,
            int edgeId
    ) {
        @JsonCreator
        public SubmissionDTO(
                @JsonProperty("submissionId") String submissionId,
                @JsonProperty("nodeId") String nodeId,
                @JsonProperty("edgeId") String edgeId
        ) {
            this(Integer.parseInt(submissionId), Integer.parseInt(nodeId), Integer.parseInt(edgeId));
        }

    }
}