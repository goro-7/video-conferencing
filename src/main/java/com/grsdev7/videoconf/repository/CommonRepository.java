package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.utils.CustomBean;
import com.grsdev7.videoconf.utils.annotations.CustomComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Optional;

import static com.grsdev7.videoconf.config.CaffeineCacheConfig.KEY_COUNT;

@CustomComponent
public abstract class CommonRepository extends CustomBean {
    protected final String cacheName;
    protected final CaffeineCache keyCache;


    protected CommonRepository(@Qualifier("keyCountCacheManager") CacheManager cacheManager, String cacheName) {
        this.keyCache = (CaffeineCache) cacheManager.getCache(KEY_COUNT);
        this.cacheName = cacheName;
    }


    protected Optional<Integer> getCurrentKey() {
        return of(keyCache.get(cacheName, Integer.class));
    }

    protected Integer getNewKey() {
        Integer newKey = of(keyCache.get(cacheName, Integer.class))
                .map(value -> ++value)
                .orElse(1);
        keyCache.put(cacheName, newKey);
        return newKey;
    }
}
