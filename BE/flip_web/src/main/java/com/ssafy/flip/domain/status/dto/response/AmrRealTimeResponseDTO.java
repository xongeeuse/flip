package com.ssafy.flip.domain.status.dto.response;

import java.util.List;

public record AmrRealTimeResponseDTO(
    List<AmrRealTimeDTO> amrRealTimeList
) {
}
