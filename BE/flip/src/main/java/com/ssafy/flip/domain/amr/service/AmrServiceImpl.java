package com.ssafy.flip.domain.amr.service;

import com.ssafy.flip.domain.amr.entity.AMR;
import com.ssafy.flip.domain.amr.repository.AmrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AmrServiceImpl implements AmrService {

    private final AmrRepository amrRepository;

    @Override
    public List<AMR> findAll(){
        return amrRepository.findAll();
    }

    @Override
    public Optional<AMR> findById(String id) {
        return amrRepository.findById(id);
    }

    @Override
    public AMR getById(String id) {
        return amrRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Can't find AMR"));
    }
}
