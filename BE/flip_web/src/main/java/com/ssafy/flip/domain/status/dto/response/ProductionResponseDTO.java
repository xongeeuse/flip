package com.ssafy.flip.domain.status.dto.response;

import java.util.List;

public record ProductionResponseDTO(
        List<ProductionDataDTO> data
) {
    public record ProductionDataDTO(
            int timestamp,
            int production,
            int target
    ) {}
}
