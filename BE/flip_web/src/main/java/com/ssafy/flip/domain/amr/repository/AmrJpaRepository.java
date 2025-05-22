package com.ssafy.flip.domain.amr.repository;

import com.ssafy.flip.domain.amr.entity.AMR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AmrJpaRepository extends JpaRepository<AMR, String> {
    @Query("SELECT COUNT(a) FROM AMR a")
    int countEntities();
}
