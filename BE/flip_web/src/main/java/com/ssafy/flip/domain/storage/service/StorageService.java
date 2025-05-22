package com.ssafy.flip.domain.storage.service;

import com.ssafy.flip.domain.storage.entity.Storage;

import java.util.Optional;

public interface StorageService {

    Optional<Storage> findById(Long id);

    Storage getStorage(Long id);
}
