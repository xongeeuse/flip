package com.ssafy.flip.domain.status.dto.response;

import java.util.List;

public record HeatMapResponseDTO(
        List<HeatMapDTO> data
) {
    public record HeatMapDTO(
            float toX,
            float toY,
            float fromX,
            float fromY,
            Long count
    ) {}
}
