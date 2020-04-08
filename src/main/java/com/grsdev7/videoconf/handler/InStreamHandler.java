package com.grsdev7.videoconf.handler;


import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.repository.UserRepository;
import com.grsdev7.videoconf.service.StreamService;
import com.grsdev7.videoconf.utils.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.netty.http.websocket.WebsocketInbound;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class InStreamHandler implements WebSocketHandler {
    public static String PATH = "/ws/send/{userId}";
    public static String OUTPUT_DIR = "data/stream";
    private final StreamService streamService;
    private final UserRepository userRepository;

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

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        User user = streamService.saveSession(session);

        Flux<WebSocketMessage> messageFlux = session.receive()
                .doFinally(signalType -> this.removeUserSession(signalType, user.getId()));

        Flux<DataBuffer> dataBufferFlux = messageFlux.map(WebSocketMessage::getPayload);

        // define  process for copying  stream to cache
        Consumer<DataBuffer> streamWriter = db -> {
            StreamWriter.transfer(db)
                    .getX()
                    .ifPresent(value -> {
                        streamService.saveStream((ByteArrayOutputStream) value, user.getId());
                    });
        };

        dataBufferFlux.subscribe(streamWriter);

        return
                Mono.never()
                        ;
    }

    private void removeUserSession(SignalType signalType, Integer userId) {
        log.debug("Client sent terminating signal : {}", signalType);
        if(signalType.equals(SignalType.ON_COMPLETE)){
            userRepository.deleteById(userId);
        }
    }

}

interface StreamWriter {
    static Tuple<Optional<OutputStream>, Optional<Exception>> transfer(DataBuffer db) {
        try (InputStream is = db.asInputStream()) {
            OutputStream os = new ByteArrayOutputStream();
            is.transferTo(os);
            return buildTuple(os, null);
        } catch (IOException ex) {
            return buildTuple(null, ex);
        }
    }

    private static Tuple<Optional<OutputStream>, Optional<Exception>> buildTuple(OutputStream os, Exception ex) {
        return new Tuple<>(ofNullable(os), ofNullable(ex));
    }
}