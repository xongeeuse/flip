package com.ssafy.flip.domain.amr.repository;

import com.ssafy.flip.domain.amr.entity.AMR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmrJpaRepository extends JpaRepository<AMR, String> {
    Optional<AMR> findById(String id);
}
