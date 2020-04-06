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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandlerImpl implements WebSocketHandler {
    public static final String FILE_TYPE = ".webm";
    public static String PATH = "ws/send";
    public static final String OUTPUT_DIR = "/Users/gaurav.salvi/Downloads/vc/input/";
    public static final Path OUTPUT_PATH = Paths.get(OUTPUT_DIR);
    private Long lastFileCreated = 0L;

    @PostConstruct
    @SneakyThrows
    public void deleteOldFiles() {
        final Path path = Paths.get(OUTPUT_DIR);
        try (Stream<Path> fileStream = Files.list(path)) {
            fileStream.forEach(path1 -> {
                try {
                    Files.deleteIfExists(path1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        Flux<WebSocketMessage> messageFlux = webSocketSession.receive();
        Flux<DataBuffer> dataFlux = messageFlux.map(WebSocketMessage::getPayload);
        Mono<String> responseMono = saveToFile((++lastFileCreated).toString() + FILE_TYPE, dataFlux);
        return webSocketSession.send(responseMono.map(webSocketSession::textMessage));
    }

    Mono<String> saveToFile(String fileName, Flux<DataBuffer> dataFlux) throws IOException {
        log.info("Starting file writing {}", fileName);
        Path path = Paths.get(OUTPUT_DIR, fileName);
        Path file = Files.createFile(path);
        WritableByteChannel channel = Files.newByteChannel(file, WRITE);
        Flux<DataBuffer> returnFlux = DataBufferUtils.write(dataFlux, channel)
                //.log()
                .doOnComplete(() -> {
                    lastFileCreated = ++lastFileCreated;
                });
        return
                returnFlux.then(Mono.just("file created : " + file.toFile().getName()));
    }

}