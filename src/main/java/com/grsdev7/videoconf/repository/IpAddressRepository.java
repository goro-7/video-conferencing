package com.grsdev7.videoconf.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class IpAddressRepository extends CommonCacheRepository {
    public final static String CACHE_NAME = "ipAddressToUserId";

    @Autowired
    public IpAddressRepository(CacheManager cacheManager) {
        super(CACHE_NAME, cacheManager);
    }

    public Optional<String> findUserIdByIp(String ipAddress) {
        return Optional.ofNullable(getCache().get(ipAddress, String.class));
    }

    public void add(String ipAddress, String userId) {
        getCache().put(ipAddress, userId);
    }
}
