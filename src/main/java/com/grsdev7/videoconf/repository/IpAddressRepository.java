package com.grsdev7.videoconf.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IpAddressRepository extends CommonRepository {
    public final static String IP_ADDRESS_TO_USER_ID = "ipAddressToUserId";
    private final CaffeineCache cache;


    public IpAddressRepository(@Qualifier("ipCacheManager") CacheManager cacheManager,
                               @Qualifier("keyCountCacheManager") CacheManager keyCacheManager) {
        super(keyCacheManager, IP_ADDRESS_TO_USER_ID);
        this.cache = (CaffeineCache) cacheManager.getCache(IP_ADDRESS_TO_USER_ID);
    }

    public Optional<Integer> findUserIdByIp(String ipAddress) {
        return Optional.ofNullable(cache.get(ipAddress, Integer.class));
    }

    public void add(String ipAddress, Integer userId) {
        cache.put(ipAddress, userId);
    }
}
