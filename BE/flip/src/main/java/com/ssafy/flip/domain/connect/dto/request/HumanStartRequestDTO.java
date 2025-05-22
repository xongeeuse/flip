package com.ssafy.flip.domain.connect.dto.request;

public record HumanStartRequestDTO(
        HumanStartRequestDTO.Header header,
        HumanStartRequestDTO.Body body
) {
    public record Header(
            String msgName,
            String time
    ) {
    }

    public record Body(
    ) {
    }
}
