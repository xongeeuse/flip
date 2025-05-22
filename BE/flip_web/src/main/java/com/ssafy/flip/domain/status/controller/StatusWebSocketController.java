package com.ssafy.flip.domain.status.controller;

import com.ssafy.flip.domain.status.service.StatusWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StatusWebSocketController {
    private final StatusWebSocketService statusWebSocketService;

    // 클라이언트가 /app/amr/{amrId}로 요청을 보내면 push 시작
    @MessageMapping("/amr/route/{amrId}")
    public void subscribeAmr(@DestinationVariable String amrId,
                             SimpMessageHeaderAccessor headerAccessor) {
        statusWebSocketService.getRouteStatus(amrId);
    }

    @MessageMapping("/amr/route/{amrId}/unsubscribe")
    public void unsubscribeAmr(@DestinationVariable String amrId,
                               SimpMessageHeaderAccessor headerAccessor) {
        statusWebSocketService.stopPushing(amrId);
    }

    // 클라이언트가 /app/amr/{amrId}로 요청을 보내면 push 시작
    @MessageMapping("/amr/human")
    public void subscribeHuman(SimpMessageHeaderAccessor headerAccessor) {
        statusWebSocketService.startHumanPushing(headerAccessor.getSessionId());
    }

    @MessageMapping("/amr/human/unsubscribe")
    public void unsubscribeHuman(SimpMessageHeaderAccessor headerAccessor) {
        statusWebSocketService.stopHumanPushing(headerAccessor.getSessionId());
    }
}
