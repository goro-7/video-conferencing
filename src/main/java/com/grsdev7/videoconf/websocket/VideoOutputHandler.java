package com.grsdev7.videoconf.websocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoOutputHandler implements WebSocketHandler {
    public static String PATH = "ws/get";
    @Value("classpath:/holi.mp4")
    private Resource resourceFile;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.trace("WebSocketSession : {}", webSocketSession.getHandshakeInfo());
        Flux<Function<DataBufferFactory, DataBuffer>> flux =
                DataBufferUtils.read(resourceFile,
                        webSocketSession.bufferFactory(),
                        1024)
                        .map(DataBufferUtils::retain)
                        .map(DataBuffer::asByteBuffer)
                        .map(byteBuffer -> (DataBufferFactory factory) -> factory.wrap(byteBuffer));

        return webSocketSession.send(flux.map(webSocketSession::binaryMessage));
    }

}

