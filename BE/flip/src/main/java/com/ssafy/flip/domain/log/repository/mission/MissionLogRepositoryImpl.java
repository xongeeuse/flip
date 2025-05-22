package com.ssafy.flip.domain.log.repository.mission;

import com.ssafy.flip.domain.log.entity.MissionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MissionLogRepositoryImpl implements MissionLogRepository {

    private final MissionLogJpaRepository missionLogJpaRepository;

    @Override
    public MissionLog save(MissionLog missionLog) {
        return missionLogJpaRepository.save(missionLog);
    }

    @Override
    public Optional<MissionLog> findById(Long id) {
        return missionLogJpaRepository.findById(id);
    }
}
