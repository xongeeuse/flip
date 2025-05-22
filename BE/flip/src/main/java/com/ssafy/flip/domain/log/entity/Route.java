package com.ssafy.flip.domain.log.entity;

import com.ssafy.flip.domain.node.entity.Edge;
import com.ssafy.flip.domain.node.entity.Node;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@EqualsAndHashCode(of = "routeId")
@Table
public class Route {

    @Id
    @Column(name = "route_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_log_id", nullable = false)
    private MissionLog missionLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private Node nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edge_id", nullable = false)
    private Edge edgeId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    public void updateStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void updateEndedAt(LocalDateTime endedAt){
        this.endedAt = endedAt;
    }
}
