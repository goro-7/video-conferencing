package com.grsdev7.videoconf.utils;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

public interface WebSocketUtils {

    static Function<DataBufferFactory,  DataBuffer> dataBufferFunction(DataBuffer value) {
        return
                factory -> factory.wrap(value.asByteBuffer());

    }

    static  WebSocketMessage toSocketMessage(WebSocketSession session, DataBuffer value) {
        WebSocketMessage message = session.binaryMessage(dataBufferFunction(value));
        return message;
    }

    static Publisher<WebSocketMessage> toSocketMessageMono(WebSocketSession session, DataBuffer value) {
        return Mono.just(toSocketMessage(session, value));
    }
}
