package com.ssafy.flip.domain.status.dto.response;

import com.ssafy.flip.domain.status.entity.AmrStatusRedis;

import java.util.ArrayList;
import java.util.List;

public record MissionStatusResponseDTO(
        List<SubmissionDTO> missionStatusList
) {
    public static MissionStatusResponseDTO from(AmrStatusRedis amrStatusRedis) {
        List<SubmissionDTO> submissionDTOList = new ArrayList<>();
        List<SubmissionDTO> submissionList = amrStatusRedis.getSubmissionList();
        int submissionId = amrStatusRedis.getSubmissionId(); // 기준 인덱스

        if(submissionId > 0) {
            for (int i = submissionId; i < submissionList.size(); i++) {
                SubmissionDTO submissionJson = submissionList.get(i);
                submissionDTOList.add(submissionJson);
            }
        }

        return new MissionStatusResponseDTO(submissionDTOList);
    }
}
