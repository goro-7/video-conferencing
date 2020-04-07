package com.grsdev7.videoconf.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
public class StreamRepository extends CommonCacheRepository {
    public final static String CACHE_NAME = "streamCache";
    private final AtomicInteger sentKey = new AtomicInteger(0);
    private final AtomicInteger currentKey = new AtomicInteger(0);
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);

    public StreamRepository(@Autowired CacheManager cacheManager) {
        super(CACHE_NAME, cacheManager);
    }

    public void saveStream(ByteArrayOutputStream stream) {
        String key = generateNewKey();
        getCache().put(key, stream);
        currentKey.set(Integer.valueOf(key));
        log.info("add to stream cache , total now : {}", key);
    }

    public Optional<ByteArrayOutputStream> getNextStreamChunk() {
        //int newKey = sentKey.incrementAndGet();
        int newKey = currentKey.intValue();
        Optional<ByteArrayOutputStream> stream = Optional.ofNullable(getCache().get(String.valueOf(newKey), ByteArrayOutputStream.class));

        if (stream.isPresent()) {
            executor.schedule(() -> {
                        var key = String.valueOf(newKey);
                        getCache().evict(key);
                        log.info("Evicted stream with key {}", key);
                    }
                    , 3,
                    SECONDS);
        }
        return stream;
    }


}
