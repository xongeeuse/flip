package com.ssafy.flip.domain.connect.dto.response;

import java.util.List;

public record MapInfoDTO (
        Header header,
        Body body
) {
    public static MapInfoDTO of(List<NodeDTO> nodes, List<EdgeDTO> edges) {
        return new MapInfoDTO(
                new Header("MAP_INFO", java.time.LocalDateTime.now().toString()),
                new Body(new MapData(10, new Areas("A_FL1", nodes, edges)))
        );
    }

    public record Header(
            String msgName,
            String time
    ) {}

    public record Body(
            MapData mapData
    ) {}

    public record MapData(
            int version,
            Areas areas
    ) {}

    public record Areas(
            String area_code,
            List<NodeDTO> nodes,
            List<EdgeDTO> edges
    ) {}
}