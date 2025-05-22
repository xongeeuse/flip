package com.ssafy.flip.domain.log.dto.error.request;

public record AddErrorRequestDTO(
        String amrId,
        String errorCode,
        Float x,
        Float y
) {
}
