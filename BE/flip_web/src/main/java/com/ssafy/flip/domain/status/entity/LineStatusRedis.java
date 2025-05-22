package com.ssafy.flip.domain.status.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("LINE_STATUS")
public class LineStatusRedis {

    @Id
    private Long lineId;

    private Float cycleTime;

    private Boolean status;

    private LocalDateTime lastInputTime;

}
