package com.ssafy.flip.domain.status.dto.response;

import com.ssafy.flip.domain.status.entity.AmrStatusRedis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AmrRealTimeDTO {
    String amrId;
    int state;
    float locationX;
    float locationY;
    float dir;
    int currentNode;
    boolean loading;
    int linearVelocity;
    String errorCode;
    String type;

    public static AmrRealTimeDTO from(AmrStatusRedis amrStatusRedis) {
        return new AmrRealTimeDTO(
                amrStatusRedis.getAmrId(),
                amrStatusRedis.getState(),
                amrStatusRedis.getX(),
                amrStatusRedis.getY(),
                amrStatusRedis.getDirection(),
                amrStatusRedis.getCurrentNode(),
                amrStatusRedis.isLoading(),
                Math.round(amrStatusRedis.getLinearVelocity()), // float -> int로 변환
                amrStatusRedis.getErrorList(),
                amrStatusRedis.getType()
        );
    }
}
