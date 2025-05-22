package com.ssafy.flip.domain.connect.service;

import com.ssafy.flip.domain.connect.dto.request.HumanSaveRequestDTO;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface HumanWebSocketService {

    void saveHuman(HumanSaveRequestDTO requestDTO);

}
