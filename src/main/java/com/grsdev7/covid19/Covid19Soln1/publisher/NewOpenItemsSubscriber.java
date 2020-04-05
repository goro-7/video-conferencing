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
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NewOpenItemsSubscriber extends BaseSubscriber<Request> {
    private final ObjectMapper objectMapper;
    private final RequestRepository requestRepository;
    private final static int INTERVAL_FLUX_MILLIS = 500;
    private final static String HEART_BEAT = "hear-beat-";
    private final @NonNull List<Subscriber<String>> subscriberList;


    public Flux<RequestDto> getFlux() {
        Instant afterTime = now().minusSeconds(5);
        //Flux<Request> requestFlux = requestRepository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(true, afterTime);

        Flux<RequestDto> requestFlux = Flux.interval(Duration.ofSeconds(1)).map(this::buildHeartBeatDto).log();
        return requestFlux.log();
        /*return
                requestFlux.map(RequestDto::fromRequest)
                        .concatWith(getIntervalFlux(INTERVAL_FLUX_MILLIS));*/
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

    public String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to conversion to json : {} , due to : {}", object, e.getMessage());
            return "{}";
        }
    }


    @Override
    public void hookOnSubscribe(Subscription subscription) {
        log.info("== Subscribed to DB with subscription: {}", subscription);
        super.requestUnbounded();
    }

    @Override
    public void hookOnNext(Request request) {
        log.info("== Publishing new DB data to subscribers : {}", request);
        sendToSubscribers(request);
    }


    @Override
    public void hookOnError(Throwable throwable) {
        log.info("== DB sent error : {}, swapping with heartbeat", throwable.getMessage());
        sendToSubscribers(buildHeartBeatDto(1L));
    }


    @Override
    public void hookOnComplete() {
        log.info("== DB sent completed : {}, swapping with heartbeat");
        sendToSubscribers(buildHeartBeatDto(2L));
    }


    private void sendToSubscribers(Request request) {
        subscriberList
                .forEach(subscriber -> subscriber.onNext(toJson(RequestDto.fromRequest(request))));
    }

    private void sendToSubscribers(RequestDto buildHeartBeatDto) {
        subscriberList
                .forEach(subscriber -> subscriber.onNext(toJson(buildHeartBeatDto)));
    }

/*    @Scheduled(fixedRate = 1000)
    public void sendHearBeats() {
        RequestDto heartBeatDto = buildHeartBeatDto(122L);
        if (subscribers.isEmpty()) log.info("== No subscriber to send heartbeat");

        subscribers.forEach(subscriber -> sendToSubscribers(heartBeatDto));
    }*/

    public void addSubscriber(Subscriber subscriber){
        this.subscriberList.add(subscriber);
    }
}

