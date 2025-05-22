package com.ssafy.flip.domain.amr.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AmrRepositoryImpl implements AmrRepository{

    private final AmrJpaRepository amrJpaRepository;

    @Override
    public int countEntities() {
        return amrJpaRepository.countEntities();
    }
}
