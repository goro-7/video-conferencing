package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);

    User save(User user);

    boolean deleteById(String id);

    void update(User user);

}
