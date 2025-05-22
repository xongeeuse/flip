package com.ssafy.flip.domain.status.service;

import com.ssafy.flip.domain.status.dto.response.AmrMissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final Map<String, AmrMissionDTO> amrMissionDTOS = new HashMap<>();

    public Map<String, AmrMissionDTO> getAmrMission() {
        return amrMissionDTOS;
    }

    public Map<String, AmrMissionDTO> setAmrMission(List<AmrMissionDTO> missionDTOList) {
        for(AmrMissionDTO amrMissionDTO : missionDTOList) {
            amrMissionDTOS.put(amrMissionDTO.amrId(), amrMissionDTO);
        }
        return amrMissionDTOS;
    }
}
