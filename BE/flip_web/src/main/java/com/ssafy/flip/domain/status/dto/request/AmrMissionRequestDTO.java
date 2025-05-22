package com.ssafy.flip.domain.status.dto.request;

public record AmrMissionRequestDTO(
        String mission,
        String amrType,
        String amrState
) {
}
