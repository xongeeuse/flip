package com.ssafy.flip.domain.connect.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AMR 경로 임시 저장 DTO
 */
@Data
@Builder
public class RouteTempDTO {
    private String missionId;
    private int submissionId;
    private int nodeId;
    private int edgeId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
