package com.ssafy.flip.domain.node.entity;

import com.ssafy.flip.domain.node.vo.EdgeDirection;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "edgeId")
@Table
public class Edge {

    @Id
    @Column(name="edge_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer edgeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "edge_direction", nullable = false)
    private EdgeDirection edgeDirection;

    @Column(nullable = false)
    private Float speed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Node node1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Node node2;
}
