package com.ssafy.flip.domain.node.repository.edge;

import com.ssafy.flip.domain.node.entity.Edge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EdgeRepositoryImpl implements EdgeRepository{

    private final EdgeJpaRepository edgeJpaRepository;

    @Override
    public List<Edge> findAll() {
        return edgeJpaRepository.findAll();
    }

    @Override
    public Optional<Edge> findById(Integer edgeId) {
        return edgeJpaRepository.findById(edgeId);
    }

    @Override
    public Edge getReferenceById(Integer edgeId) {
        return edgeJpaRepository.getReferenceById(edgeId);
    }

}
