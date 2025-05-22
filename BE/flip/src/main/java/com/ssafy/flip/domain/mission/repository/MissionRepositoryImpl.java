package com.ssafy.flip.domain.mission.repository;

import com.ssafy.flip.domain.mission.entity.Mission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepository {

    private final MissionJpaRepository missionJpaRepository;

    @Override
    public Optional<Mission> findById(String missionId) {
        return missionJpaRepository.findById(missionId);
    }
}
