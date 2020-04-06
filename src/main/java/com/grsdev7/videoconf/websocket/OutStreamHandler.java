package com.grsdev7.videoconf.websocket;


import com.grsdev7.videoconf.events.CustomSpringEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.grsdev7.videoconf.websocket.InStreamHandler.FILE_TYPE;
import static com.grsdev7.videoconf.websocket.InStreamHandler.OUTPUT_DIR;
import static java.nio.file.StandardOpenOption.READ;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutStreamHandler implements WebSocketHandler, ApplicationListener<CustomSpringEvent> {
    public static String PATH = "ws/get";
    private AtomicLong lastFileSent = new AtomicLong(1L);


    @PostConstruct
    public void setup() {

    }

    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("WebSocketSession : {}", webSocketSession.getHandshakeInfo());

        // find latest file to be sent
        Path newPath = Paths.get(OUTPUT_DIR, lastFileSent.toString() + FILE_TYPE);

        Optional<Path> newFileOpt = Files.exists(newPath) ? Optional.of(newPath) : Optional.empty();

        // send latest file
        if (newFileOpt.isEmpty()) {
            log.info("Do not have new file");
            return webSocketSession.send(Mono.empty());
        } else {
            Path newFile = newFileOpt.get();
            log.info("sending stream from file {}", newFile);
            Flux<WebSocketMessage> flux = DataBufferUtils.read(newFile,
                    webSocketSession.bufferFactory(),
                    (int) Files.size(newFile))
                    .map(dataBuffer -> dataBuffer.asByteBuffer())
                    .map(byteBuffer -> (Function<DataBufferFactory, DataBuffer>) (DataBufferFactory dbf) -> dbf.wrap(byteBuffer))
                    .map(webSocketSession::binaryMessage)
                    .doOnComplete(() -> {
                        lastFileSent.incrementAndGet();
                        log.info("file sent fully - {}", newFile);
                    });
            return
                    webSocketSession.send(flux)
                            .doFinally(signal -> webSocketSession.close());
        }
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


    @Override
    public void onApplicationEvent(CustomSpringEvent customSpringEvent) {
        log.info("Received application event {}", customSpringEvent.getMessage());
        DataBuffer message = customSpringEvent.getMessage();
    }
}

