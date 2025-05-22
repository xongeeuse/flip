package com.ssafy.flip.domain.storage.service;

import com.ssafy.flip.domain.storage.entity.Storage;
import com.ssafy.flip.domain.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;

    @Override
    public Optional<Storage> findById(Long id) {
        return storageRepository.findById(id);
    }

    @Override
    public Storage getStorage(Long id) {
        return storageRepository.findById(id)
                .orElseThrow();
    }
}
