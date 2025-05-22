package com.ssafy.flip.domain.log.service.missionlog;

import com.ssafy.flip.domain.log.entity.MissionLog;
import com.ssafy.flip.domain.log.repository.missionlog.MissionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionLogServiceImpl implements MissionLogService {

    private final MissionLogRepository missionRepository;

    @Override
    public Optional<MissionLog> findById(Long id) {
        return missionRepository.findById(id);
    }

    @Override
    public MissionLog getMission(Long id) {
        return missionRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<MissionLog> findBeforeHour(int hour) {
        return missionRepository.findByEndedAtBefore(LocalDateTime.now().minusHours(hour));
    }

    @Override
    public List<MissionLog> findRecentMissionLogsByMissionIds(List<String> missionIds, LocalDateTime thresholdTime) {
        return missionRepository.findRecentMissionLogsByMissionIds(missionIds, thresholdTime);
    }

}
