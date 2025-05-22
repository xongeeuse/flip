package com.ssafy.flip.domain.node.service.edge;

import com.ssafy.flip.domain.node.entity.Edge;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EdgeService {

    List<Edge> findAll();

    Optional<Edge> findById(Integer edgeId);

    Edge getEdge(Integer edgeId);

    Map<String, String> getEdgeKeyToIdMap();

}
