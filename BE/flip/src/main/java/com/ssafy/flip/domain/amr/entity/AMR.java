package com.ssafy.flip.domain.amr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "amrId")
@Table(name = "amr")
public class AMR {
    @Id
    @Column(name = "amr_id", length = 8)
    private String amrId;

    @Column(nullable = false, length = 16)
    private String name;

    @Column(nullable = false)
    private String type;

    @Builder.Default
    @Column(name = "installed_at", nullable = false)
    private LocalDateTime installedAt = LocalDateTime.now();
}
