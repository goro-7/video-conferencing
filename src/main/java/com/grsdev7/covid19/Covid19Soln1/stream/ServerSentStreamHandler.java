package com.grsdev7.covid19.Covid19Soln1.stream;

import com.grsdev7.covid19.Covid19Soln1.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerSentStreamHandler {
    private final RequestService requestService;
    private final SSEPublisher publisher;

    public Mono<ServerResponse> getRequestSSE(ServerRequest serverRequest) {
        log.info("## Got request for SSE : {}", serverRequest.headers());

        Flux<ServerSentEvent<String>> sourceFlux = Flux.from(publisher);
        ParameterizedTypeReference<ServerSentEvent<String>> typeRef = new ParameterizedTypeReference<ServerSentEvent<String>>() {
        };
        return
                ServerResponse.ok()
                        .contentType(TEXT_EVENT_STREAM)
                        .body(sourceFlux, typeRef);
    }




/* working but with separate db calls
    public Mono<ServerResponse> getRequestSSE(ServerRequest serverRequest) {
        Instant time = LocalDateTime.now().minusDays(3).toInstant(ZoneOffset.UTC);

        Flux<RequestDto> numFlux = Flux.interval(Duration.ofSeconds(10))
                .flatMap(num -> requestService.getAllByActiveAndCreatedOnGreaterThan(true, time));
        return
                ServerResponse.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(numFlux, Long.class);
    }
*/
}
