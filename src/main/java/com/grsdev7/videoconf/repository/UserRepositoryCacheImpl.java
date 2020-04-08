package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class UserRepositoryCacheImpl extends CommonRepository implements UserRepository {
    public static final String USERS = "users";
    private final CaffeineCache cache;
    private final Collection<Integer> activeUserIdList = new ConcurrentLinkedQueue<>();

    public UserRepositoryCacheImpl(@Qualifier("userCacheManager") CacheManager cacheManager,
                                   @Qualifier("keyCountCacheManager") CacheManager keyCacheManager) {
        super(keyCacheManager, USERS);
        this.cache = (CaffeineCache) cacheManager.getCache(USERS);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return ofNullable(cache.get(id, User.class));
    }

    @Override
    public User save(User user) {
        cache.put(user.getId(), user);
        activeUserIdList.add(user.getId());
        log.info("User saved : {}", user);
        return user;
    }

    @Override
    public void update(User user) {
        cache.put(user.getId(), user);
        log.info("User updated : {}", user);
    }

    @Override
    public List<User> findAllUsersOtherThan(Integer userId) {
        return
                activeUserIdList.stream()
                        .filter(id -> !id.equals(userId))
                        .map(id -> cache.get(id, User.class))
                        .collect(toList());
    }

    @Override
    public void removeUserFromActiveList(Integer userId) {
        activeUserIdList.remove(userId);
    }


    @Override
    public boolean deleteById(Integer id) {
        log.trace("Removing user : {}", id);
        return cache.evictIfPresent(id);
    }

}
