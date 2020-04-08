package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Integer id);

    User save(User user);

    boolean deleteById(Integer id);

    void update(User user);

    List<User> findAllUsersOtherThan(Integer userId);

    void removeUserFromActiveList(Integer key);
}
