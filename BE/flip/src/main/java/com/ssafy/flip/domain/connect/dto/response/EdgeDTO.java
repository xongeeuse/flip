package com.ssafy.flip.domain.connect.dto.response;

import com.ssafy.flip.domain.node.entity.Edge;

public record EdgeDTO(
        Integer edgeId,
        int node1,
        int node2,
        String edgeDirection,
        float speed
) {
    public static EdgeDTO from(Edge edge) {
        return new EdgeDTO(
                edge.getEdgeId(),
                edge.getNode1().getNodeId(),
                edge.getNode2().getNodeId(),
                edge.getEdgeDirection().getValue(),
                edge.getSpeed()
        );
    }
}