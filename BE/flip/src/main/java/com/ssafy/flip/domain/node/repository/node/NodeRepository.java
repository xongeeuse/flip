package com.ssafy.flip.domain.node.repository.node;

import com.ssafy.flip.domain.node.entity.Node;

import java.util.List;
import java.util.Optional;

public interface NodeRepository {

    List<Node> findAll();

    Optional<Node> findById(Integer nodeId);

    Node getReferenceById(Integer nodeId);

}
