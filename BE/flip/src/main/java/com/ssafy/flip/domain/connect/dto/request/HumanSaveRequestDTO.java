package com.ssafy.flip.domain.connect.dto.request;

public record HumanSaveRequestDTO(
        HumanSaveRequestDTO.Header header,
        HumanSaveRequestDTO.Body body
) {
    public record Header(
            String msgName,
            String time
    ) {
    }

    public record Body(
            String humanId,
            float worldX,
            float worldY,
            float dir,
            int state
    ) {
    }
}
