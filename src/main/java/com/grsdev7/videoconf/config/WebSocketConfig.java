package com.grsdev7.videoconf.config;

import com.grsdev7.videoconf.handler.InStreamHandler;
import com.grsdev7.videoconf.handler.OutStreamHandler;
import com.grsdev7.videoconf.service.MediaProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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
public class WebSocketConfig {
    private final InStreamHandler sendStreamHandler;
    private final OutStreamHandler outStreamHandler;


    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = Map.of(
                outStreamHandler.PATH, outStreamHandler,
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

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MediaProcessor mediaProcessor() {
        return MediaProcessor.newInstance();
    }
}
