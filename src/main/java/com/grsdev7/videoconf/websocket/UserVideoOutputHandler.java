package com.grsdev7.videoconf.websocket;


import com.grsdev7.videoconf.events.CustomSpringEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserVideoOutputHandler implements WebSocketHandler, ApplicationListener<CustomSpringEvent> {
    public static String PATH = "ws/get";

    @PostConstruct
    public void setup() {
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("WebSocketSession : {}", webSocketSession.getHandshakeInfo());

        Flux<String> flux = Flux.interval(Duration.ofSeconds(1))
                .map(id -> id.toString());

        //Flux<Function<DataBufferFactory, DataBuffer>> flux =

        //.log()
        //.flatMap(dataBuffer -> Mono.just(DataBufferUtils.retain(dataBuffer)))
        //.map(DataBuffer::asByteBuffer)
        //.map(byteBuffer -> (DataBufferFactory factory) -> factory.wrap(byteBuffer));
        return webSocketSession.send(flux.map(webSocketSession::textMessage));
    }

    @Override
    public void onApplicationEvent(CustomSpringEvent customSpringEvent) {
        log.info("Received application event {}", customSpringEvent.getMessage());
        DataBuffer message = customSpringEvent.getMessage();
    }
}

