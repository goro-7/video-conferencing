package com.grsdev7.covid19.Covid19Soln1.stream;

import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.service.RequestService;
import com.grsdev7.covid19.Covid19Soln1.utils.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.time.ZoneOffset.UTC;

@RequiredArgsConstructor
@Slf4j
@Component
public class SSEPublisher implements Publisher<ServerSentEvent<String>> {
    private final Collection<Subscriber<? super ServerSentEvent<String>>> subscriberList = new ConcurrentLinkedQueue<Subscriber<? super ServerSentEvent<String>>>();
    private final Collection<Disposable> heartBeatDisposableList = new ConcurrentLinkedQueue<>();
    private int cancelledSubscriptions = 0;
    private final JsonMapper jsonMapper;
    private final RequestService requestService;


    @Override
    public void subscribe(Subscriber<? super ServerSentEvent<String>> subscriber) {
        log.info("## New subscription request : " + subscriber);
        subscriberList.add(subscriber);
        log.info("## Added new subscriber, total subscribers now : {}", subscriberList.size());
    }

    //scan db and publish new records
    //@Scheduled(fixedRate = 10000)
    public void scanDBAndPublish() {
        log.info("## Scanning DB");
        Instant afterTime = LocalDateTime.now().minusDays(10).toInstant(UTC);

        // remove all subs
/*        subscriberList.stream()
                .filter(subscriber -> {
                    subscriber.onComplete();
                    return false;
                }).collect(toList());*/

        heartBeatDisposableList.forEach(disposable -> {
            log.info("Heartbeat running ? : {}" + disposable.isDisposed());
            log.info("Cancelling heartbeats");
            // disposable.dispose();
        });

        heartBeatDisposableList.clear();
        Flux<RequestDto> dbFlux = requestService.getAllByActiveAndCreatedOnGreaterThan(true, afterTime);
        Flux<RequestDto> sharableDbFlux = dbFlux.share();

        // this works using Flux.create
        ParallelFlux<ServerSentEvent<String>> sourceFlux = Flux.create(sink -> {
            Disposable newDisposable = Flux.from(sharableDbFlux)
                    .doFinally(signal -> handleFinally(signal))
                    .subscribe(
                            data -> {
                                log.info("Got data from DB : {}", data);
                                sink.next(data);
                            },
                            error -> sink.next(buildHeartBeat("error")),
                            () -> {
                                log.info("Db complete event will be switched to heartbeats");
                                Disposable disposable = Flux.interval(Duration.ofSeconds(1))
                                        .doOnComplete(() -> clearSubs())
                                        .subscribe(num -> sink.next(buildHeartBeat("idle-" + num.toString())));
                                heartBeatDisposableList.add(disposable);
                            }
                    );

            sink.onCancel(this::handleOnCancel);
            sink.onCancel(this::handleOnCancel);
            sink.onRequest(this::consumeRequestDemand);
        }).map(this::toServerSentEvent)
                .parallel();

        subscriberList.forEach(
                subscriber -> {
                    Flux.from(sourceFlux)
                            .subscribe(subscriber);
                }
        );




        /*          approach works **
                Flux<RequestDto> heartBeatFlux = Flux.interval(Duration.ofSeconds(20))
                .map(this::buildHeartBeat);
      Flux<ServerSentEvent> infiniteFlux = dbFlux.flatMap(null,
                null,
                () -> heartBeatFlux)
                .map(this::toServerSentEvent)
                .share()
                .log();*/



        /*heartBeatFlux.subscribe(
                data -> {
                    log.info("## Got Data from source");
                    send(data);
                }
        );*/
        log.info("Returning from scanDBAndPublish ");
    }

    private void clearSubs() {
        log.info("Heart beat complete, clearing subscriptions");
        subscriberList.clear();
    }

    private void consumeRequestDemand(long count) {
        log.info("Sink is being asked for request count of {}", count);
    }

    private synchronized void handleOnCancel() {
        cancelledSubscriptions = cancelledSubscriptions + 1;
        log.info("Total closed subscribers : {}", cancelledSubscriptions);
       /* if (cancelledSubscriptions >= subscriberList.size()) {
            subscriberList.clear();
            log.info("Clearing subscriberList");
            cancelledSubscriptions = 0;
        }*/
    }

    private void handleFinally(SignalType signal) {
        log.info("Got finally signal : {}", signal);
    }

    private void send(RequestDto data) {
        if (subscriberList.isEmpty()) {
            log.warn("## Got Data but subscriberList is empty");
        } else {
            log.info("## Sending data to {} subscribers : {}", subscriberList.size(), data);
            subscriberList.forEach(subscriber ->
                    {
                        log.info("## Sending to data : {}, subscriber : {}", data, subscriber);
                        subscriber.onNext(toServerSentEvent(data));
                    }
            );
        }
    }

    private RequestDto buildHeartBeat(String suffix) {
        return RequestDto.builder()
                .id("heart-beat-" + suffix + "-thread-" + Thread.currentThread().getName())
                .build();
    }

    private ServerSentEvent<String> toServerSentEvent(Object data) {
        return ServerSentEvent.<String>builder()
                .id(UUID.randomUUID().toString())
                .event("message")
                .data(jsonMapper.toJson(data))
                .comment("open request items")
                .retry(Duration.ofSeconds(10))
                .build();
    }
}
