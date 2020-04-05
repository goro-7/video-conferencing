package com.grsdev7.covid19.Covid19Soln1.repository;


import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface RequestRepository extends ReactiveMongoRepository<Request, String> {

    Flux<Request> findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(boolean active, Instant createdOn);
}
