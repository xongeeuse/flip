package com.ssafy.flip.domain.node.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "wallId")
@Table
public class VirtualWall {

    @Id
    @Column(name = "wall_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wallId;

    @Column(nullable = false)
    private Float x1;

    @Column(nullable = false)
    private Float y1;

    @Column(nullable = false)
    private Float x2;

    @Column(nullable = false)
    private Float y2;

}
