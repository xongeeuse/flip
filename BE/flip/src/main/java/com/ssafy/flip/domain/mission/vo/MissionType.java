package com.ssafy.flip.domain.mission.vo;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum MissionType {
    MOVING("MOVING"),
    CHARGING("CHARGING"),
    LOADING("LOADING"),
    UNLOADING("UNLOADING");

    private final String value;

    @JsonCreator
    public static MissionType fromValue(String value){
        if (value == null) {
            throw new IllegalArgumentException("Invalid status: null");
        }
        String upperValue = value.toUpperCase();
        return Stream.of(MissionType.values())
                .filter(status -> status.value.equals(upperValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
    }
}
