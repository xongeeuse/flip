package com.ssafy.flip.domain.status.entity;

import com.ssafy.flip.domain.status.dto.response.RouteDTO;
import com.ssafy.flip.domain.status.dto.response.SubmissionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("AMR_STATUS")
public class AmrStatusRedis {

    @Id
    private String amrId;

    private float x;
    private float y;
    private float direction;

    private int state;
    private int battery;
    private boolean loading;
    private float linearVelocity;

    private int currentNode;
    private String missionId;
    private String missionType;
    private int submissionId;

    private String errorList;

    private String type;

    //submissionId, submissionNode, submissionX, submissionY로 구성
    private List<SubmissionDTO> submissionList;
    //지나간 루트를 표시
    //routeId(순서), routeNode, startedAt으로 구성
    private List<RouteDTO> routeList;
}
