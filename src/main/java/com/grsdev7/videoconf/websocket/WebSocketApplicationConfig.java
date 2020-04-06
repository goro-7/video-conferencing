package com.grsdev7.videoconf.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.Map;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketApplicationConfig {
    private final WebSocketHandlerImpl sendStreamHandler;
    private final VideoOutputHandler getStreamHandler;
    private final UserVideoOutputHandler userVideoOutputHandler;


    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = Map.of(userVideoOutputHandler.PATH, userVideoOutputHandler,
                sendStreamHandler.PATH, sendStreamHandler
        );
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(HIGHEST_PRECEDENCE);
        log.info("Handler mapping : {}", mapping);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        ReactorNettyRequestUpgradeStrategy upgradeStrategy = new ReactorNettyRequestUpgradeStrategy();
        upgradeStrategy.setMaxFramePayloadLength(Integer.MAX_VALUE);
        return new HandshakeWebSocketService(upgradeStrategy);
    }
}
