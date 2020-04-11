package com.grsdev7.videoconf.handler;


import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.repository.UserRepository;
import com.grsdev7.videoconf.service.MediaProcessor;
import com.grsdev7.videoconf.service.StreamService;
import com.grsdev7.videoconf.utils.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.write;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.SignalType.CANCEL;
import static reactor.core.publisher.SignalType.ON_COMPLETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class InStreamHandler implements WebSocketHandler {
    public static String PATH = "/ws/send/{userId}";
    private final StreamService streamService;
    private final UserRepository userRepository;
    private final ApplicationContext applicationContext;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        User user = saveUserSessionIfNew(session);
        log.info("Got client message from {}", user.getId());

        Flux<WebSocketMessage> messageFlux = session.receive()
                .doFinally(signalType -> this.removeUserSession(signalType, user.getId()));

        Flux<DataBuffer> clientFlux = messageFlux.map(WebSocketMessage::getPayload);

        clientFlux.subscribe(user.getProcessor());

        attachToOtherClientStream(user);

/* This we need to try
        // define  process for copying  stream to cache
        Consumer<DataBuffer> streamWriter = db -> {
            StreamWriter.transfer(db)
                    .getX()
                    .ifPresent(value -> {
                        streamService.saveStream((ByteArrayOutputStream) value, user.getId());
                    });
        };

        MediaProcessor<DataBuffer> mediaProcessor = MediaProcessor.newInstance();
        clientFlux.subscribe(mediaProcessor);*/


        //clientFlux.subscribe(streamWriter); this also works

        return
                Mono.never()
                ;
    }

    private void attachToOtherClientStream(User newUser) {
        List<User> otherUsers = getOtherUsers(newUser.getId());
        otherUsers
                .forEach(existingUsers -> {
                    // attach existingUsers to processor of new newUser
                    newUser.getProcessor().attachDownStream(existingUsers.getSession());
                    // attach new user to processor of  existingUser
                    existingUsers.getProcessor().attachDownStream(newUser.getSession());
                });
    }

    private List<User> getOtherUsers(String userId) {
        return userRepository.findAllUsers()
                .stream()
                .filter(cachedUser -> !cachedUser.getId().equals(userId))
                .collect(toList());
    }

    private Publisher<WebSocketMessage> toWebSocketMessage(WebSocketSession session, Flux<DataBuffer> upstreamFlux) {
        return upstreamFlux.map(data -> session.binaryMessage(factory -> data));
    }

    private EnumSet<SignalType> terminalSignals = EnumSet.of(ON_COMPLETE, CANCEL);

    private void removeUserSession(SignalType signalType, String userId) {
        log.debug("Client sent terminating signal : {}", signalType);
        if (terminalSignals.contains(signalType)) {
            userRepository.deleteById(userId);
            getOtherUsers(userId).forEach(user -> {
                user.getProcessor().removeDownStream(userId);
            });
        }
    }

    public User saveUserSessionIfNew(WebSocketSession webSocketSession) {
        ReactorNettyWebSocketSession session = (ReactorNettyWebSocketSession) webSocketSession;
        String userId = getUserId(session);
        User user = getUserSession(userId).orElseGet(() -> createNewUserWithSession(userId, session));
        return user;
    }

    private User createNewUserWithSession(String userId, WebSocketSession session) {
        MediaProcessor processor = applicationContext.getBean(MediaProcessor.class);
        User user = User.builder()
                .id(userId)
                .session(session)
                .processor(processor)
                .build();
        processor.setUser(user);
        return userRepository.save(user);
    }

    private Optional<User> getUserSession(String userId) {
        return userRepository.findById(userId);
    }

    private String getUserId(WebSocketSession session) {
        ReactorNettyWebSocketSession nettySession = (ReactorNettyWebSocketSession) session;
        URI uri = nettySession.getHandshakeInfo().getUri();
        UriTemplate template = new UriTemplate("/ws/send/{userId}");
        Map<String, String> parameters = template.match(uri.getPath());
        return parameters.get("userId");
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