package com.ssafy.flip.domain.status.dto.response;

import com.ssafy.flip.domain.status.entity.LineStatusRedis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record LineStatusResponseDTO(
        List<LineStatusDTO> lineList,
        LocalDateTime timestamp
) {
    public record LineStatusDTO(
            Long lineId,
            int amount,
            boolean status
    ) {
        public static LineStatusDTO from(LineStatusRedis line, int amount){
            return new LineStatusDTO(
                    line.getLineId(),
                    amount,
                    line.getStatus());
        }
    }

    public static LineStatusResponseDTO from(Map<LineStatusRedis, Integer> lineAmountMap) {
        List<LineStatusDTO> dtoList = lineAmountMap.entrySet().stream()
                .map(entry -> LineStatusDTO.from(entry.getKey(), entry.getValue()))
                .toList();
        return new LineStatusResponseDTO(dtoList, LocalDateTime.now());
    }

}
