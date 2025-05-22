package com.ssafy.flip.domain.status.dto.request;


import java.util.List;

public record AmrSaveRequestDTO(
        Header header,
        Body body
) {
    public record Header(
            String msgName,
            String time
    ) {}

    public record Body(
            float worldX,
            float worldY,
            float dir,
            String amrId,
            int state,
            int battery,
            int currentNode,
            int currentEdge,
            boolean loading,
            String missionId,
            String missionType,
            int submissionId,
            float linearVelocity,
            String errorList,
            List<String> routeList
    ) {}
}
