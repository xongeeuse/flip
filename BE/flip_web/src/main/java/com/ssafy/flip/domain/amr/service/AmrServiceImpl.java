package com.ssafy.flip.domain.amr.service;

import com.ssafy.flip.domain.amr.repository.AmrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmrServiceImpl implements AmrService{

    private final AmrRepository amrRepository;

    @Override
    public int countEntities() {
        return amrRepository.countEntities();
    }
}
