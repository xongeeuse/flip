package com.ssafy.flip.domain.line.repository;

import com.ssafy.flip.domain.line.entity.Line;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LineRepositoryImpl implements LineRepository {

    private final LineJpaRepository storageJpaRepository;

    @Override
    public List<Line> findAll() {
        return storageJpaRepository.findAll();
    }

    @Override
    public Optional<Line> findById(Long id) {
        return storageJpaRepository.findById(id);
    }
}
