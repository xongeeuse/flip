package com.ssafy.flip.domain.status.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("HUMAN_STATUS")
public class HumanStatusRedis {

    @Id
    private String humanId;

    private float x;
    private float y;
    private float direction;

    private int state;

}
