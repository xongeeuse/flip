package com.ssafy.flip.domain.storage.repository;

import com.ssafy.flip.domain.amr.entity.AMR;
import com.ssafy.flip.domain.storage.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StorageJpaRepository extends JpaRepository<Storage, Long> {
    Optional<Storage> findById(Long id);
}
