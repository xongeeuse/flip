package com.ssafy.flip.domain.status.dto.response;

import java.time.LocalDateTime;

public record FactoryStatusResponseDTO(
        int amrMaxNum,
        int amrWorking,
        int amrWaiting,
        int amrCharging,
        int amrError,
        int amrWorkTime,
        int lineMaxNum,
        int lineWorking,
        int storageQuantity,
        int storageMaxQuantity,
        LocalDateTime timestamp
) {
}
