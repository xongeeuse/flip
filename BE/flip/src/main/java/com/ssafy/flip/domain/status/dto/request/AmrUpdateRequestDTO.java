package com.ssafy.flip.domain.status.dto.request;

import java.util.List;

public record AmrUpdateRequestDTO(
        String amrId,
        List<String> submissionList
) {
}
