package com.grsdev7.covid19.Covid19Soln1.publisher;

import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

import static com.grsdev7.covid19.Covid19Soln1.utils.TestUtil.buildRequestWithIdList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = {NewOpenItemsPeriodPublisher.class})
public class NewOpenItemsPeriodPublisherTest {
    @Autowired
    private NewOpenItemsPeriodPublisher periodPublisher;

    @MockBean
    private RequestRepository repository;

    @Test
    public void shouldReturnInfiniteFlux() {
        // given
        given(repository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(eq(true), any(Instant.class)))
                .willReturn(Flux.fromIterable(buildRequestWithIdList(5)));

        // when
        Flux<RequestDto> requestDtoFlux = periodPublisher.getFlux().log();

        // then
        assertThat(requestDtoFlux).isNotNull();
        StepVerifier.create(requestDtoFlux)
                .expectNextCount(20)
                .thenCancel()
                .verify();
    }
}
