package com.grsdev7.videoconf.repository;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static java.util.Optional.ofNullable;

public abstract class CommonCacheRepository {
    public static final String CACHE_KEYS = "cache-keys";
    protected CacheManager cacheManager;
    private final Cache cache;
    private final String cacheName;

    protected CommonCacheRepository(String cacheName, CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
        cache = cacheManager.getCache(cacheName);
    }

    protected Cache getCache() {
        return cache;
    }

    protected String generateNewKey() {
        Cache keysCache = cacheManager.getCache(CACHE_KEYS);
        String id = getCurrentKey();
        String newId = ofNullable(id)
                .map(Integer::valueOf)
                .map(oldId -> ++oldId)
                .map(num -> num.toString())
                .orElse("1");
        keysCache.put(cacheName, newId);
        return newId;
    }

    public String getCurrentKey(){
        Cache keysCache = cacheManager.getCache(CACHE_KEYS);
        return keysCache.get(cacheName, String.class);
    }

}

