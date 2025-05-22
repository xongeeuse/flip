package com.ssafy.flip.global.listener;
import com.ssafy.flip.domain.status.service.StatusWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final StatusWebSocketService statusWebSocketService;

    // 세션 끊길 때 AMR 전송 중지 로직
    @EventListener
    public void onDisconnect(org.springframework.web.socket.messaging.SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // sessionId -> amrId 매핑 관리하고 있었다면 여기에 해제 로직 추가
        // 예: sessionToAmrMap.get(sessionId)로 amrId 조회 후 stopPushing(amrId)
    }
}
