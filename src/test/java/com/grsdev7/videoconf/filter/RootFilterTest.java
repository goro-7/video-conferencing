package com.grsdev7.videoconf.filter;

import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class RootFilterTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldCreateClientObjectIfNotExists() {
        // given
        client.get()
                .uri("/")
                .exchange()
                .expectHeader().valueEquals("userId", "1")
                .expectStatus()
                .isOk();

        // when
        Optional<User> user = userRepository.findById("1");

        // then
        assertThat(user).isPresent();
    }
}
