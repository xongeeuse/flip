package com.ssafy.flip.domain.mission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MissionResponse {

    private String amrId;
    private String missionId;
    private String missionType;
    private int expectedArrival;
    private List<Integer> route;

    public MissionResponse(String amrId, String missionId, String missionType, int expectedArrival, List<Integer> route) {
        this.amrId = amrId;
        this.missionId = missionId;
        this.missionType = missionType;
        this.expectedArrival = expectedArrival;
        this.route = route;
    }
}
