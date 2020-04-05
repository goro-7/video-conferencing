package com.grsdev7.covid19.Covid19Soln1.controller.repository;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.grsdev7.covid19.Covid19Soln1.utils.TestUtil.buildRequest;
import static com.grsdev7.covid19.Covid19Soln1.utils.TestUtil.buildRequestList;
import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
//@ActiveProfiles("staging")
@ExtendWith(SpringExtension.class)
public class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void shouldSaveRequest() {
        // given
        Request request = buildRequest();
        // when
        Mono<Request> savedRequest = requestRepository.save(request);

        //then
        Flux<Request> dbResult = requestRepository.findAllById(savedRequest.map(Request::getId));
        StepVerifier.create(dbResult)
                .assertNext(request1 -> assertAreEqual(request, request1))
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldReturnRequestsActiveAndSortedWithTail() {
        // given
        List<Request> savedRequests = loadData();

        // when
        Example<Request> probe = Example.of(Request.builder().active(true).build());
        Sort sort = Sort.by("createdOn").descending();
        Flux<Request> requestFlux = requestRepository.findAll(probe, sort)
                .limitRequest(2);

        // then
        StepVerifier.create(requestFlux)
                .assertNext(request -> verify(savedRequests.get(0), request))
                .assertNext(request -> verify(savedRequests.get(1), request))
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldReturnRequestAsPerCreatedDate() throws InterruptedException {
        // given
        List<Request> oldRequests = loadData();
        TimeUnit.SECONDS.sleep(6);
        List<Request> newRequests = loadData();

        // when
        Instant createdOn = Instant.now().minusSeconds(4);
        Flux<Request> requestFlux = requestRepository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(true, createdOn);

        // then
        StepVerifier.create(requestFlux)
                .expectNextCount(newRequests.size())
                .expectComplete()
                .log()
                .verify();
    }

    private List<Request> loadData() {
        List<Request> requestList = buildRequestList(10);
        return requestRepository.saveAll(requestList)
                .sort(Comparator.comparing(Request::getCreatedOn).reversed())
                .collect(toList())
                .block();
    }

    private void verify(Request request, Request request1) {
        out.println("request : " + request);
        out.println("request1 : " + request1);
        assertThat(request1).isEqualToIgnoringGivenFields(request, "createdOn");
    }

    private void assertAreEqual(Request request, Request request1) {
        assertThat(request1).isEqualToIgnoringGivenFields(request, "id", "createdOn");
        assertThat(request1.getCreatedOn()).isNotNull();
    }
}
