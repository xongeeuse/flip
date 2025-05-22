package com.ssafy.flip.domain.storage.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = "storageId")
@Table
public class Storage {
    @Id
    @Column(name = "storage_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int maxAmount;
}
