package com.grsdev7.videoconf.websocket;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandlerImpl implements WebSocketHandler {
    public static final String FILE_TYPE = ".webm";
    public static String PATH = "ws/send";
    public static final String OUTPUT_DIR = "/Users/gaurav.salvi/Downloads/vc/input/";

    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        Flux<WebSocketMessage> messageFlux = webSocketSession.receive();
        Flux<DataBuffer> dataFlux = messageFlux.map(WebSocketMessage::getPayload);
        Mono<String> responseMono = saveToFile("test-"+ UUID.randomUUID().toString() + FILE_TYPE, dataFlux);
        return webSocketSession.send(responseMono.map(webSocketSession::textMessage));
    }

    Mono<String> saveToFile(String fileName, Flux<DataBuffer> dataFlux) throws IOException {
        log.info("Starting file writing {}", fileName);
        Path path = Paths.get(OUTPUT_DIR, fileName);
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Path file = Files.createFile(path);
        WritableByteChannel channel = Files.newByteChannel(file, WRITE);
        Flux<DataBuffer> returnFlux = DataBufferUtils.write(dataFlux, channel).log();
        return
                returnFlux.then(Mono.just("file created : "+file.toFile().getName()));
    }

}