package com.ssafy.flip.domain.status.dto.request;

import org.springframework.data.annotation.Id;

import java.util.List;

public record AmrSaveRequestDTO(
        String amrId,
        float x,
        float y,
        float direction,
        int state,
        int battery,
        boolean loading,
        float linearVelocity,
        int currentNode,
        String missionId,
        String missionType,
        int submissionId,
        String errorList,
        String type,
        float startX,
        float startY,
        float targetX,
        float targetY,
        int expectedArrival,
        List<String>submissionList,
        List<String> routeList
) {
}
