package com.grsdev7.covid19.Covid19Soln1.publisher;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Item;
import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.domain.request.Requestor;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class PeriodicDBScanner {
    private final RequestRepository requestRepository;
    private  final ApplicationContext context;

    //@Scheduled(fixedDelay = 10000000)
    public void scanDBAndPublish() {
        log.info("== Scanning DB");
        Instant afterTime = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC);
        //Flux<Request> dbFlux = requestRepository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(true, afterTime);
        Flux<Request> dbFlux = Flux.interval(Duration.ofSeconds(1))
                .map(num -> createRequest(num));
        dbFlux.subscribe(context.getBean(NewOpenItemsSubscriber.class));
    }

    private Request createRequest(Long num) {
        return Request.builder().id("heart-beat-"+ num )
                .requestor(Requestor.builder().build())
                .items(Set.of(Item.builder().build()))
                .active(true)
                .build();
    }
}
