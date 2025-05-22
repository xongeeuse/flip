package com.ssafy.flip.domain.mission.service;

import com.ssafy.flip.domain.mission.entity.Mission;
import com.ssafy.flip.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {

    private final MissionRepository missionRepository;

    @Override
    public Optional<Mission> findById(String missionId) {
        return missionRepository.findById(missionId);
    }

    @Override
    public Mission getMission(String missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow();
    }

}
