package com.ssafy.flip.domain.status.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDTO {
    int routeId;
    int routeNode;
    String startedAt;

    @JsonCreator
    public RouteDTO(
            @JsonProperty("RouteId") int routeId,
            @JsonProperty("RouteNode") int routeNode,
            @JsonProperty("startedAt") String startedAt
    ) {
        this.routeId = routeId;
        this.routeNode = routeNode;
        this.startedAt = startedAt;
    }
}
