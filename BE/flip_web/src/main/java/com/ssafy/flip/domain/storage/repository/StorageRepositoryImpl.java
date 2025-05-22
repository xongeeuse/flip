package com.ssafy.flip.domain.storage.repository;

import com.ssafy.flip.domain.storage.entity.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StorageRepositoryImpl implements StorageRepository {

    private final StorageJpaRepository storageJpaRepository;

    @Override
    public Optional<Storage> findById(Long id) {
        return storageJpaRepository.findById(id);
    }
}
