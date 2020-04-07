package com.grsdev7.videoconf.repository;

import com.grsdev7.videoconf.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldSaveUser() {
        // given
        User user = User.builder()
                .ipAddress("123.123")
                .build();

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved).isEqualToIgnoringNullFields(user);
    }

    @Test
    public void shouldFindUser() {
        // given
        User user = User.builder()
                .ipAddress("123.123")
                .build();
        User saved = userRepository.save(user);

        // when
        User fromRepo = userRepository.findById(saved.getId()).get();

        // then
        assertThat(saved).isEqualTo(fromRepo);
    }

    @Test
    public void shouldUpdateUser() {
        // given
        User user = User.builder()
                .ipAddress("123.123")
                .build();
        User saved = userRepository.save(user);
        User newUser = saved.withIpAddress("new.ip.add");

        // when
        userRepository.update(newUser);
        var updated = userRepository.findById(saved.getId());

        // then
        assertThat(updated.get()).isEqualTo(newUser);
    }

    @Test
    public void shouldDeleteUser() {
        // given
        User user = User.builder()
                .ipAddress("123.123")
                .build();
        User saved = userRepository.save(user);

        // when
        userRepository.deleteById(saved.getId());
        var updated = userRepository.findById(saved.getId());

        // then
        assertThat(updated).isEmpty();
    }
}
