package com.ssafy.flip.domain.status.service;

import com.ssafy.flip.domain.connect.dto.request.AmrMissionDTO;
import com.ssafy.flip.domain.status.dto.request.AmrSaveRequestDTO;
import com.ssafy.flip.domain.status.dto.request.AmrUpdateRequestDTO;
import com.ssafy.flip.domain.status.dto.request.LineSaveRequestDTO;
import com.ssafy.flip.domain.status.dto.request.MissionRequestDto;

import java.util.List;

public interface StatusService {

    void saveAmr(AmrSaveRequestDTO amrDto, List<String> routeList);

    void updateSubmissionList(String amrId, List<String> submissionList);

    void updateRouteList(String amrId, List<String> submissionList);

    void saveLine(Integer lineId);

    void repairLine();

    void brokeLine();
}
