package com.ssafy.flip.domain.log.repository.missionlog;

import com.ssafy.flip.domain.log.entity.MissionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionLogJpaRepository extends JpaRepository<MissionLog, Long> {

    List<MissionLog> findAll();

    Optional<MissionLog> findById(Long id);

    @Query("""
    SELECT m FROM MissionLog m
    JOIN m.mission mi
    WHERE m.endedAt >= :startTime
    AND mi.missionType <> 'MOVING'
    """)
    List<MissionLog> findByEndedAtBefore(LocalDateTime startTime);

    @Query("SELECT ml FROM MissionLog ml " +
            "WHERE ml.mission.missionId IN :missionIds " +
            "AND ml.endedAt >= :thresholdTime")
    List<MissionLog> findRecentMissionLogsByMissionIds(
            @Param("missionIds") List<String> missionIds,
            @Param("thresholdTime") LocalDateTime thresholdTime);
}
