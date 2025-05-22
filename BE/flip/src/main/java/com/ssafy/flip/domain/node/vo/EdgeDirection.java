package com.ssafy.flip.domain.node.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum EdgeDirection {
    FORWARD("FORWARD"),
    REARWARD("REARWARD"),
    TWOWAY("TWOWAY");

    private String value;

    @JsonCreator
    public static EdgeDirection fromValue(String value){
        if (value == null) {
            throw new IllegalArgumentException("Invalid status: null");
        }
        String upperValue = value.toUpperCase();
        return Stream.of(EdgeDirection.values())
                .filter(status -> status.value.equals(upperValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
    }
}
