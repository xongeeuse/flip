package com.ssafy.flip.domain.status.dto.request;

public record LineSaveRequestDTO(
        Integer lineId,
        Float cycleTime,
        Boolean status
) {
}
