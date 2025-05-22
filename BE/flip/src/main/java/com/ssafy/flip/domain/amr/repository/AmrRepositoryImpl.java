package com.ssafy.flip.domain.amr.repository;

import com.ssafy.flip.domain.amr.entity.AMR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AmrRepositoryImpl implements AmrRepository {
    private final AmrJpaRepository amrJpaRepository;

    @Override
    public List<AMR> findAll() {
        return amrJpaRepository.findAll();
    }

    @Override
    public Optional<AMR> findById(String id) {
        return amrJpaRepository.findById(id);
    }
}
