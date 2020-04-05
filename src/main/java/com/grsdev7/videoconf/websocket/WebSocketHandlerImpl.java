package com.grsdev7.videoconf.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandlerImpl implements WebSocketHandler {
    public static String PATH = "ws";
    private final ObjectMapper objectMapper;
    @Value("classpath:/video.mp4")
    private Resource resourceFile;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("WebSocketSession : {}", webSocketSession.getHandshakeInfo());
        // request
        Mono<Void> monoFromInput = webSocketSession.receive()
                .doOnNext(message -> log.info("== Received message : {}", message.getPayloadAsText()))
                .then();

        Flux<Function<DataBufferFactory, DataBuffer>> sourceFlux = DataBufferUtils.read(resourceFile,
                webSocketSession.bufferFactory(),
                429496729)
                .map(dataBuffer -> dataBuffer.asByteBuffer())
                .log()
                .map(byteBuffer -> (DataBufferFactory dbf) -> dbf.wrap(byteBuffer));

        Mono<Void> outputMono = webSocketSession.send(sourceFlux.map(webSocketSession::binaryMessage));

        // return response
        // Mono<Void> outputMono = webSocketSession.send(sourceFlux.map(webSocketSession::textMessage));

        return Mono.zip(monoFromInput, outputMono).then();
    }


}
