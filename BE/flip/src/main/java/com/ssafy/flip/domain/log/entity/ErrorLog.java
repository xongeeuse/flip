package com.ssafy.flip.domain.log.entity;

import com.ssafy.flip.domain.amr.entity.AMR;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@EqualsAndHashCode(of = "errorLogId")
@Table(name = "amr_error_log")
public class ErrorLog {

    @Id
    @Column(name = "error_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long errorLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amr_id")
    private AMR amr;

    @Column(nullable = false)
    private String errorCode;

    @Column(nullable = false)
    private Float x;

    @Column(nullable = false)
    private Float y;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "resolved_at", nullable = false)
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        this.occurredAt = LocalDateTime.now();
    }

    public void updateResolvedAt(LocalDateTime resolvedAt){
        this.resolvedAt = resolvedAt;
    }

}
