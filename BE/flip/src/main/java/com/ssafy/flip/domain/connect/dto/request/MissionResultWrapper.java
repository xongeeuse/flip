package com.ssafy.flip.domain.connect.dto.request;

import com.ssafy.flip.domain.mission.dto.MissionResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MissionResultWrapper {
    private String triggeredAmr;
    private List<MissionResponse> missions;
}
