package com.grsdev7.videoconf.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.grsdev7.videoconf.repository.UserRepository;
import com.grsdev7.videoconf.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.time.Duration;

import static com.grsdev7.videoconf.repository.IpAddressRepository.IP_ADDRESS_TO_USER_ID;
import static com.grsdev7.videoconf.repository.UserRepositoryCacheImpl.USERS;
import static com.grsdev7.videoconf.service.StreamService.STREAM;

@Slf4j
@Configuration
public class CaffeineCacheConfig {
    public final static String KEY_COUNT = "keyCount";
    @Autowired
    @Lazy
    private StreamService streamService;

    @Bean
    public CaffeineCacheManager ipCacheManager() {
        Caffeine<Object, Object> caffeine =
                Caffeine.newBuilder()
                        .initialCapacity(10)
                        .maximumSize(20)
                        .expireAfterWrite(Duration.ofDays(1))
                        .recordStats()
                        .removalListener((key, value, cause) -> log.info("Removed entry from {}, key {} due to {}",
                                IP_ADDRESS_TO_USER_ID, key, cause));
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(IP_ADDRESS_TO_USER_ID);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager userCacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(20)
                .expireAfterWrite(Duration.ofDays(1))
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.info("Removed entry from {}, key {} due to {}", USERS, key, cause);
                    streamService.removeUserFromActiveList((Integer) key);
                });

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(USERS);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager streamCacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(100)
                //.expireAfterAccess(Duration.ofSeconds(20))
                .recordStats()
                .removalListener((key, value, cause) -> log.info("Removed entry from {}, key {} due to {}",
                        STREAM, key, cause));

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(STREAM);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    @Bean
    @Primary
    public CaffeineCacheManager keyCountCacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(20)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.info("Removed entry from {}, key {} due to {}", KEY_COUNT, key, cause);
                });
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(KEY_COUNT);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
