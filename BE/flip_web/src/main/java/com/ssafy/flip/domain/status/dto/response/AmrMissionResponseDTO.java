package com.ssafy.flip.domain.status.dto.response;

import com.ssafy.flip.domain.status.entity.AmrStatusRedis;

import java.time.Duration;
import java.time.LocalDateTime;

public record AmrMissionResponseDTO(
    String amrId,
    int state,
    String missionId,
    String missionType,
    int submissionId,
    String errorCode,
    LocalDateTime timestamp,
    int battery,
    String type,
    float startX,
    float startY,
    float targetX,
    float targetY,
    int expectedArrival,
    Long realArrival
) {
    public static AmrMissionResponseDTO from(AmrStatusRedis amrStatusRedis, AmrMissionDTO missionDTO) {
        int submissionId = amrStatusRedis.getSubmissionId();
        boolean isSubmissionIdInvalid = (submissionId == 0);

        float startX = isSubmissionIdInvalid ? 0F : missionDTO.startX();
        float startY = isSubmissionIdInvalid ? 0F : missionDTO.startY();
        float targetX = isSubmissionIdInvalid ? 0F : missionDTO.targetX();
        float targetY = isSubmissionIdInvalid ? 0F : missionDTO.targetY();

        return new AmrMissionResponseDTO(
                amrStatusRedis.getAmrId(),
                amrStatusRedis.getState(),
                amrStatusRedis.getMissionId(),
                missionDTO.type(),
                submissionId,
                amrStatusRedis.getErrorList(),
                LocalDateTime.now(),
                amrStatusRedis.getBattery(),
                amrStatusRedis.getType(),
                startX,
                startY,
                targetX,
                targetY,
                missionDTO.expectedArrival(),
                Duration.between(missionDTO.startedAt(), LocalDateTime.now()).getSeconds()
        );
    }

}
