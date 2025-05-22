package com.ssafy.flip.domain.storage.repository;

import com.ssafy.flip.domain.storage.entity.Storage;

import java.util.Optional;

public interface StorageRepository {
    Optional<Storage> findById(Long id);
}
