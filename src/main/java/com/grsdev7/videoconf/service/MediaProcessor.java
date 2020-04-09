package com.grsdev7.videoconf.service;

import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.utils.WebSocketUtils;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.grsdev7.videoconf.utils.WebSocketUtils.toSocketMessageMono;
import static reactor.core.Disposables.never;

@Slf4j
public class MediaProcessor<T extends DataBuffer> extends BaseSubscriber<T> {
    @Nullable
    private FluxSink<T> sink;

    private User user; //user to whose stream this processor is attached to for influx

    private final List<WebSocketSession> downstreamList = new ArrayList<>();

    private MediaProcessor() {

    }

    public static <T extends DataBuffer> MediaProcessor<T> newInstance() {
        MediaProcessor<T> instance = new MediaProcessor<>();
        log.debug("New media processor instance created {}", instance);
        return instance;
    }

    public void attachSink(@NonNull FluxSink<T> sink) {
        this.sink = sink;
        this.sink.onCancel(() -> {
            super.cancel();
            this.sink = null;
        });
        log.info("Downstream sink attached : {}", sink);
    }

    @Override
    protected void hookOnNext(T value) {
        if (sink != null) {
            sink.next(value);
        }
        downstreamList.forEach(
                session -> {
                    session.send(toSocketMessageMono(session, value))
                            .log()
                            .doFinally(result -> log.info("type - {}", result))
                            .subscribe();
                }
        );
        requestUnbounded();
    }

    @Override
    protected void hookOnComplete() {
        log.info("Recieved complete from upstream ");
        if (sink != null) {
            sink.complete();
        }
        downstreamList.forEach(session -> session.close().then());
    }

    @Override
    protected void hookOnError(Throwable t) {
        log.warn("Got error {}", t.getMessage());
        if (sink != null) {
            sink.error(t);
        }
        downstreamList.forEach(session -> session.close().then());
    }

    public FluxSink<T> getSink() {
        return sink;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        log.info("Upstream media publisher accepted subscription : {}", subscription);
        requestUnbounded();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Publisher<T> attachDownStream(WebSocketSession session) {
        downstreamList.add(session);
        log.info("Added new downstream for userId {} : {}", user, session);
        return Mono.<T>never();
    }

    public void removeDownStream(String userId) {
        downstreamList.remove(userId);
    }

    @Override
    public String toString() {
        return String.format("[MediaProcessor userId:%s,  downstreams size : %d]",
                user.getId(), downstreamList.size());
    }
}
