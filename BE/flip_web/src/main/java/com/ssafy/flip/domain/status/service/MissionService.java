package com.ssafy.flip.domain.status.service;

import com.ssafy.flip.domain.status.dto.response.AmrMissionDTO;

import java.util.List;
import java.util.Map;

public interface MissionService {

    Map<String, AmrMissionDTO> getAmrMission();

    Map<String, AmrMissionDTO> setAmrMission(List<AmrMissionDTO> missionDTO);

}
