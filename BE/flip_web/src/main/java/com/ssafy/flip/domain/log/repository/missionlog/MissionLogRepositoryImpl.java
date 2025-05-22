package com.ssafy.flip.domain.log.repository.missionlog;

import com.ssafy.flip.domain.log.entity.MissionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MissionLogRepositoryImpl implements MissionLogRepository {

    private final MissionLogJpaRepository missionLogJpaRepository;

    @Override
    public List<MissionLog> findAll() {
        return missionLogJpaRepository.findAll();
    }

    @Override
    public Optional<MissionLog> findById(Long id) {
        return missionLogJpaRepository.findById(id);
    }

    @Override
    public List<MissionLog> findByEndedAtBefore(LocalDateTime endTime) {
        return missionLogJpaRepository.findByEndedAtBefore(endTime);
    }

    @Override
    public List<MissionLog> findRecentMissionLogsByMissionIds(List<String> missionIds, LocalDateTime thresholdTime) {
        return missionLogJpaRepository.findRecentMissionLogsByMissionIds(missionIds, thresholdTime);
    }

}
