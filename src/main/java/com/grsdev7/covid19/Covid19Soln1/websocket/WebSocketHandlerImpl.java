package com.grsdev7.covid19.Covid19Soln1.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grsdev7.covid19.Covid19Soln1.publisher.NewOpenItemsPeriodPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
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
    public static final String MEDIA_FILE = "/Users/gaurav.salvi/Downloads/holi.mp4";
    public static String PATH = "ws";
    private final NewOpenItemsPeriodPublisher publisher;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("WebSocketSession : {}", webSocketSession.getHandshakeInfo());
        // request
        Mono<Void> monoFromInput = webSocketSession.receive()
                .doOnNext(message -> log.info("== Received message : {}", message.getPayloadAsText()))
                .then();

        // response
/*        Flux<String> sourceFlux = //Flux.from(publisher).log();
                Flux.interval(Duration.ofSeconds(1))
                        .map(num -> num.toString())
                        .log();*/


        Flux<Function<DataBufferFactory, DataBuffer>> sourceFlux = DataBufferUtils.read(new FileSystemResource(Paths.get(MEDIA_FILE)),
                webSocketSession.bufferFactory(),
                429496729)
                .map(dataBuffer -> dataBuffer.asByteBuffer())
                .log()
                .map(byteBuffer -> (DataBufferFactory dbf) -> dbf.wrap(byteBuffer))
                ;

        Mono<Void> outputMono = webSocketSession.send(sourceFlux.map(webSocketSession::binaryMessage));

        // return response
        // Mono<Void> outputMono = webSocketSession.send(sourceFlux.map(webSocketSession::textMessage));

        return Mono.zip(monoFromInput, outputMono).then();
    }


}
