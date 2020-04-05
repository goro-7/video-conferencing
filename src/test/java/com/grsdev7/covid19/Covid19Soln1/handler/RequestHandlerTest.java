package com.grsdev7.covid19.Covid19Soln1.handler;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import com.grsdev7.covid19.Covid19Soln1.router.RouterFunctionsConfig;
import com.grsdev7.covid19.Covid19Soln1.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.grsdev7.covid19.Covid19Soln1.utils.TestUtil.buildRequestWithIdList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@SpringJUnitConfig(classes = {RequestService.class, RequestHandler.class, RouterFunctionsConfig.class, RootPathRequestHandler.class})
@WebFluxTest
public class RequestHandlerTest {

    private WebTestClient client;

    @MockBean
    private RequestRepository requestRepository;

    @BeforeEach
    void setUp(ApplicationContext context) {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void shouldReturnActiveRequests() {
        // given
        Flux<Request> dbRequestFlux = Flux.fromIterable(buildRequestWithIdList(30));
        Mockito.when(requestRepository.findAll(any(), any())).thenReturn(dbRequestFlux);

        // when
        client.mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build()
                .get()
                .uri("/requests?active=true&sortBy=createdOn&limit=10")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RequestDto.class)
                .returnResult()
                .getResponseBody()
                .forEach(response -> {
                    assertThat(response).hasNoNullFieldsOrProperties();
                });

    }

}
