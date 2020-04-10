package com.grsdev7.videoconf.handler;


import com.grsdev7.videoconf.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Callable;

import static java.nio.file.StandardOpenOption.READ;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutStreamHandler implements WebSocketHandler {
    public static String PATH = "ws/get";
    private final StreamService streamService;

    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("WebSocketSession : {}", webSocketSession.getHandshakeInfo());
        Mono<ByteArrayOutputStream> streamMono = streamService.getNextStreamChunk();
        Mono<WebSocketMessage> response = streamMono.map(value -> webSocketSession.binaryMessage(dbf -> dbf.wrap(value.toByteArray())));
        return
                webSocketSession.send(response)
                        .then();
    }


    @SneakyThrows
    private Callable<AsynchronousFileChannel> getByteChannel(Path file) {
        return () -> AsynchronousFileChannel.open(file, READ);
    }

    private FileTime extractLastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file);
        } catch (IOException e) {
            e.printStackTrace();
            return FileTime.fromMillis(1);
        }
    }


}

