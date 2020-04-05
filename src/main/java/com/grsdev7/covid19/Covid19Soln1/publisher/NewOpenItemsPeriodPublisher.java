package com.grsdev7.covid19.Covid19Soln1.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;

@Slf4j
@RequiredArgsConstructor
@Component
public class NewOpenItemsPeriodPublisher implements Publisher<String> {
    private final ObjectMapper objectMapper;
    private final RequestRepository requestRepository;
    private final static int INTERVAL_FLUX_MILLIS = 500;
    private final static String HEART_BEAT = "hear-beat-";
    private final List<Subscriber<String>> subscriberList;

    public Flux<RequestDto> getFlux() {
        Instant afterTime = now().minusSeconds(5);
        Flux<Request> requestFlux = requestRepository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(true, afterTime);
        return
                requestFlux.map(RequestDto::fromRequest)
                        .concatWith(getIntervalFlux(INTERVAL_FLUX_MILLIS));
    }

    public Flux<String> toJsonFlux(Flux<?> flux) {
        return flux.map(this::convertToJson);
    }

    public String toJson(Object object) {
        return convertToJson(object);
    }

    private Publisher<? extends RequestDto> getIntervalFlux(int intervalFluxMillis) {
        return
                Flux.interval(Duration.ofMillis(intervalFluxMillis))
                        .map(this::buildHeartBeatDto)
                ;
    }

    public RequestDto buildHeartBeatDto(Long num) {
        return RequestDto.builder()
                .id(HEART_BEAT + num)
                .createdOn(now())
                .build();
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to conversion to json : {} , due to : {}", object, e.getMessage());
            return "{}";
        }
    }

    @Override
    public void subscribe(@NonNull Subscriber subscriber) {
        subscriberList.add(subscriber);
        log.info("== Added new subscriber : {}", subscriber);
    }

}

