package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class UserRepositoryCachImpl extends CommonCacheRepository implements UserRepository {
    public static final String USERS = "users";

    @Autowired
    public UserRepositoryCachImpl(CacheManager cacheManager) {
        super(USERS, cacheManager);
    }

    @Override
    public Optional<User> findById(String id) {
        return ofNullable(getCache().get(id, User.class));
    }

    @Override
    public User save(User user) {
        user = user.withId(generateNewKey());
        getCache().put(user.getId(), user);
        log.info("User saved : {}", user);
        return user;
    }

    @Override
    public void update(User user) {
        getCache().put(user.getId(), user);
        log.info("User saved : {}", user);
    }


    @Override
    public boolean deleteById(String id) {
        return getCache().evictIfPresent(id);
    }

}
