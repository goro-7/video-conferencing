package com.grsdev7.covid19.Covid19Soln1.service;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.grsdev7.covid19.Covid19Soln1.utils.TestUtil.buildRequestWithIdList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @InjectMocks
    private RequestService requestService;
    @Mock
    private RequestRepository requestRepository;

    @Test
    public void shouldReturnActiveSortedRequests() {
        // given
        Flux<Request> dbRequestFlux = Flux.fromIterable(buildRequestWithIdList(3));
        Mockito.when(requestRepository.findAll(any(), any())).thenReturn(dbRequestFlux);

        // when
        Flux<RequestDto> requests = requestService.getRequests(null, null, 2);

        // then
        StepVerifier.create(requests)
                .assertNext(this::verifyRequestDto)
                .assertNext(this::verifyRequestDto)
                .expectComplete()
                .verify();

    }

    @Test
    public void shouldReturnLatest10ActiveRequests() {
        // given
        Flux<Request> dbRequestFlux = Flux.fromIterable(buildRequestWithIdList(20));
        Mockito.when(requestRepository.findAll(any(), any())).thenReturn(dbRequestFlux);

        // when
        Flux<RequestDto> requests = requestService.getRequests(true, "createdOn", 10);

        // then
        StepVerifier.create(requests)
                .assertNext(this::verifyRequestDto)
                .assertNext(this::verifyRequestDto)
                .expectNextCount(8)
                .expectComplete()
                .verify();

    }

    private void verifyRequestDto(RequestDto requestDto) {
        assertThat(requestDto).isNotNull().hasNoNullFieldsOrProperties();
    }

}
