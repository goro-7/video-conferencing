package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);

    User save(User user);

    boolean deleteById(String id);

    void update(User user);

    List<User> findAllUsers();

    void removeUserFromActiveList(String key);
}
