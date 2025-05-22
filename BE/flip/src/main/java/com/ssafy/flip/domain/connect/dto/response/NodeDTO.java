package com.ssafy.flip.domain.connect.dto.response;

import com.ssafy.flip.domain.node.entity.Node;

public record NodeDTO (
        Integer nodeId,
        String nodeName,
        String nodeType,
        Float worldX,
        Float worldY,
        Float direction
){
    public static NodeDTO from(Node node) {
        return new NodeDTO(
                node.getNodeId(),
                node.getNodeName(),
                node.getNodeType().name(),
                node.getX(),
                node.getY(),
                node.getDirection()
        );
    }
}
