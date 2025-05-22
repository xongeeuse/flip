package com.ssafy.flip.domain.mission.entity;

import com.ssafy.flip.domain.mission.vo.MissionType;
import com.ssafy.flip.domain.node.entity.Node;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "missionId")
@Table
public class Mission {

    @Id
    @Column(name = "mission_id")
    private String missionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false)
    private MissionType missionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id", nullable = false)
    private Node node;

}
