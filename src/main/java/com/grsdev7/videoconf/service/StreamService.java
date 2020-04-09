package com.grsdev7.videoconf.service;

import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.repository.UserRepository;
import com.grsdev7.videoconf.repository.UserRepositoryCacheImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.grsdev7.videoconf.utils.WebSocketUtils.toSocketMessageMono;

@RequiredArgsConstructor
@Slf4j
@Service
public class StreamService {
    public final static String STREAM = "stream";
    @Qualifier("streamCacheManager")
    @Autowired
    private CaffeineCacheManager cacheManager;
    private CaffeineCache cache;
    private CaffeineCache userCache;
    private final AtomicInteger lastSavedKey = new AtomicInteger(0);
    private final AtomicInteger lastSentKey = new AtomicInteger(0);

    @Lazy
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void setUp() {
        this.cache = (CaffeineCache) cacheManager.getCache(STREAM);
        this.userCache = (CaffeineCache) cacheManager.getCache(UserRepositoryCacheImpl.USERS);
    }

/*    public void saveStream(ByteArrayOutputStream stream, Integer userId) {
        int key = lastSavedKey.incrementAndGet();
        cache.put(key, stream);
        sendStreamToClients(stream, userId);
        log.debug("Added stream to with id : {}", key);
    }*/

/*    private void sendStreamToClients(ByteArrayOutputStream stream, Integer userId) {
        userRepository.findAllUsers()
                .filter(user -> !user.getId().equals(userId))
                .map(user -> user.getSession())
                .parallel()
                .map(session -> session.send(toSocketMessageMono(session, stream)))
                .doOnError(error -> log.warn("Failed to send video stream due to {}", error.getMessage()));

*//* this works
        WebSocketSession session = user.getSession();
        Mono<WebSocketMessage> message = Mono.just(stream)
                .map(value -> session.binaryMessage(dbf -> dbf.wrap(value.toByteArray())));
        session.send(message)
                .doOnError(ex -> {
                    log.warn("Error occurred on send data due to {}", ex.getMessage());
                    userRepository.removeUserFromActiveList(user.getId());
                })
                .subscribe();*//*
    }*/

    public Mono<ByteArrayOutputStream> getNextStreamChunk() {
        int newKey = lastSavedKey.get();

        Cache.ValueWrapper valueWrapper = null;
        int count = 0;
        do {
            count++;
            valueWrapper = cache.get(newKey);
            ++newKey;
        } while (valueWrapper == null && count < 3);

        return Mono.justOrEmpty(valueWrapper)
                .map(value -> ((ByteArrayOutputStream) value.get()));
    }




/*
    public void removeUserFromActiveList(String key) {
        userRepository.removeUserFromActiveList(key);
    }
*/


}

