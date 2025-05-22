package com.ssafy.flip.domain.node.repository.edge;

import com.ssafy.flip.domain.node.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EdgeJpaRepository extends JpaRepository<Edge, Integer> {

    List<Edge> findAll();

    Optional<Edge> findById(Integer edgeId);

}
