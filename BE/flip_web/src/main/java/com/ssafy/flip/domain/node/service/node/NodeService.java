package com.ssafy.flip.domain.node.service.node;

import com.ssafy.flip.domain.node.entity.Node;

import java.util.List;
import java.util.Optional;

public interface NodeService {

    List<Node> findAll();

    Optional<Node> findById(Integer nodeId);

    Node getNode(Integer nodeId);

    Node getNodeFromCache(int nodeId);

}
