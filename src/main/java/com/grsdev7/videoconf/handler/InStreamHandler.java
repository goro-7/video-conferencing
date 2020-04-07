package com.grsdev7.videoconf.handler;


import com.grsdev7.videoconf.repository.StreamRepository;
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
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
@RequiredArgsConstructor
public class InStreamHandler implements WebSocketHandler {
    public static final String FILE_TYPE = ".webm";
    public static String PATH = "ws/send";
    public static String OUTPUT_DIR = "data/stream";
    private AtomicLong lastFileCreated = new AtomicLong(0L);
    private final StreamRepository streamRepository;


    @PostConstruct
    public void createOutputDir() throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR);
        try {
            Files.createDirectories(outputDir);
        } catch (FileAlreadyExistsException e) {
            Files.deleteIfExists(outputDir);
            Files.createDirectories(outputDir);
        }
    }

    @PreDestroy
    @SneakyThrows
    public void deleteOldFiles() {
        log.info("deleting all stream files");
        try (Stream<Path> fileStream = Files.list(Paths.get(OUTPUT_DIR))) {
            fileStream.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
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


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        return
                DataBufferUtils.write(dataFlux, os)
                        .doOnComplete(() -> {
                                    log.info("data written to os - {}", os.size());
                                    streamRepository.saveStream(os);
                                }
                        )
                        .then(Mono.empty())
                ;


        // return mono.then();
        /* this works
        Mono<String> responseMono = saveToFile(lastFileCreated.incrementAndGet() , FILE_TYPE, dataFlux);
        return webSocketSession.send(responseMono.map(webSocketSession::textMessage));
         */
    }

    Mono<String> saveToFile(Long fileNumber, String fileType, Flux<DataBuffer> dataFlux) throws IOException {

        Path path = Paths.get(OUTPUT_DIR, fileNumber.toString() + fileType);
        Path file = Files.createFile(path);
        log.info("Starting file writing - {}", file);
        WritableByteChannel channel = Files.newByteChannel(file, WRITE);
        Flux<DataBuffer> returnFlux = DataBufferUtils.write(dataFlux, channel)
                .doOnComplete(() -> log.info("file written - {}", file));


        return
                returnFlux.then(Mono.just("file created : " + file.toFile().getName()));
    }

}