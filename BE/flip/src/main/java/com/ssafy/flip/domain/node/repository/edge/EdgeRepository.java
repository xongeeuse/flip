package com.ssafy.flip.domain.node.repository.edge;

import com.ssafy.flip.domain.node.entity.Edge;

import java.util.List;
import java.util.Optional;

public interface EdgeRepository {

    List<Edge> findAll();

    Optional<Edge> findById(Integer edgeId);

    Edge getReferenceById(Integer edgeId);

}
