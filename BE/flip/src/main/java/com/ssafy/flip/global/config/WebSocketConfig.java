package com.ssafy.flip.global.config;

import com.ssafy.flip.domain.connect.handler.AmrWebSocketHandler;
import com.ssafy.flip.domain.connect.handler.HumanWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AmrWebSocketHandler amrHandler;

    @Autowired
    private HumanWebSocketHandler humanHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(amrHandler, "/ws/amr").setAllowedOrigins("*");
        registry.addHandler(humanHandler, "/ws/human").setAllowedOrigins("*");
    }

    /**
     * JSR-356 기반 서버 컨테이너(예: Tomcat) 메시지 버퍼 사이즈 설정
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 1024);    // 텍스트 최대 1MB 버퍼 :contentReference[oaicite:0]{index=0}
        container.setMaxBinaryMessageBufferSize(512 * 1024);   // 바이너리 최대 512KB 버퍼
        return container;
    }

    /**
     * Tomcat WebSocket 구현체를 위한 context-param 설정
     */
    @Bean
    public ServletContextInitializer websocketBufferSizeConfig() {
        return servletContext -> {
            servletContext.setInitParameter(
                    "org.apache.tomcat.websocket.textBufferSize",
                    String.valueOf(1024 * 1024)
            );
            servletContext.setInitParameter(
                    "org.apache.tomcat.websocket.binaryBufferSize",
                    String.valueOf(512 * 1024)
            );
        };
    }
}