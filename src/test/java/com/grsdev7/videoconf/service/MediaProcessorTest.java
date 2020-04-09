package com.grsdev7.videoconf.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MediaProcessorTest {

    private MediaProcessor<DataBuffer> mediaProcessor;

    @BeforeEach
    public void setUp() {
        mediaProcessor = MediaProcessor.newInstance();
    }

    @Test
    public void shouldPassOnDataFromUpstream() {
        // given
        Flux<DataBuffer> upStreamFlux = Flux.interval(ofMillis(100))
                .map(num -> buildDefaultDataBuffer());

        // when
        upStreamFlux.subscribe(mediaProcessor); // attaching upstream clients to accept flux
        Flux<DataBuffer> mediaProcessorFlux = Flux.<DataBuffer>create(mediaProcessor::attachSink).log(); // passing on flux for downstream clients

        // then
        mediaProcessorFlux.subscribe();
        StepVerifier.create(mediaProcessorFlux)
                .expectNextCount(3)
                .thenCancel()
                .verify();
    }

    public static DataBuffer buildDefaultDataBuffer() {
        return new DefaultDataBufferFactory().allocateBuffer();
    }

    @Test
    public void shouldHandleLimitedFlux() {
        // given
        List<DataBuffer> source = buildDataBufferList();
        Flux<DataBuffer> upStreamFlux = Flux.fromIterable(source).delayElements(Duration.ofSeconds(1)).
                log();

        // when
        upStreamFlux.subscribe(mediaProcessor); // attaching upstream clients to accept flux
        Flux<DataBuffer> mediaProcessorFlux = Flux.<DataBuffer>create(mediaProcessor::attachSink) // passing on flux for downstream clients
                .log();

        // then
        StepVerifier.create(mediaProcessorFlux)
                .assertNext(source::contains)
                .assertNext(source::contains)
                .assertNext(source::contains)
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldPassOnError() {
        // given
        Flux<DataBuffer> upStreamFlux = Flux.just(buildDefaultDataBuffer(), null)
                .delayElements(Duration.ofMillis(500))
                .log();

        // when
        upStreamFlux.subscribe(mediaProcessor); // attaching upstream clients to accept flux
        Flux<DataBuffer> mediaProcessorFlux = Flux.<DataBuffer>create(mediaProcessor::attachSink) // passing on flux for downstream clients
                .log();

        // then
        StepVerifier.create(mediaProcessorFlux)
                .expectNextCount(1)
                .expectError(NullPointerException.class)
                .verify(Duration.ofMillis(1000));
    }

    @Test
    public void shouldRespectCancel() throws InterruptedException {
        // given
        Flux<DataBuffer> upStreamFlux = Flux.interval(ofMillis(100))
                .map(num -> buildDefaultDataBuffer())
                .log();

        // when
        upStreamFlux.subscribe(mediaProcessor); // attaching upstream clients to accept flux
        Flux<DataBuffer> mediaProcessorFlux = Flux.<DataBuffer>create(mediaProcessor::attachSink) // passing on flux for downstream clients
                .log();

        // then
        StepVerifier.create(mediaProcessorFlux)
                .expectNextCount(2)
                .thenCancel()
                .verify();


        assertThat(mediaProcessor.getSink()).isNull();
    }

    public static List<DataBuffer> buildDataBufferList() {
        return of(buildDefaultDataBuffer(), buildDefaultDataBuffer(), buildDefaultDataBuffer());
    }
}