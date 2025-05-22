package com.ssafy.flip.domain.line.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "lineId")
@Table
public class Line {
    @Id
    @Column(name = "line_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lineId;

    @Column(name = "cycle_time", nullable = false)
    private Float cycleTime;

    @Column(nullable = false)
    private boolean status;

    public void brokeLine() {
        this.status = false;
    }

    public void repairLine() {
        this.status = true;
    }
}
