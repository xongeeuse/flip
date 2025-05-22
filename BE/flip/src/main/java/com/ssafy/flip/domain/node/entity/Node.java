package com.ssafy.flip.domain.node.entity;

import com.ssafy.flip.domain.node.vo.NodeType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "nodeId")
@Table
public class Node {

    @Id
    @Column(name = "node_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nodeId;

    @Column(name = "node_name", nullable = false)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(nullable = false)
    private Float x;

    @Column(nullable = false)
    private Float y;

    @Column
    private Float direction;

    @Column(name = "geo_fence", nullable = false)
    private Boolean geoFence;

}
