package com.ssafy.flip.domain.log.entity;

import com.ssafy.flip.domain.amr.entity.AMR;
import com.ssafy.flip.domain.mission.entity.Mission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@EqualsAndHashCode(of = "missionLogId")
@Table(name = "mission_log")
public class MissionLog {

    @Id
    @Column(name = "mission_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amr_id", nullable = false)
    private AMR amr;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    public void updateStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void updateEndedAt(LocalDateTime endedAt){
        this.endedAt = endedAt;
    }
}
