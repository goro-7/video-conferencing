package com.grsdev7.covid19.Covid19Soln1.handler;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestHandler {
    private final RequestService requestService;

    public Mono<ServerResponse> saveNewRequest(ServerRequest serverRequest) {
        Mono<RequestDto> requestDto = convert(serverRequest);
        Mono<Request> requestMono = requestService.saveRequest(requestDto);
        log.info("Returning : {}", requestMono);
        return convert(requestMono);
    }

    public Mono<ServerResponse> getRequests(ServerRequest serverRequest) {
        log.info("Request : {}", serverRequest);
        boolean active = serverRequest.queryParam("active").map(Boolean::parseBoolean).orElse(false);
        String sortBy = serverRequest.queryParam("sortBy").orElse("createdOn");
        int resultSize = serverRequest.queryParam("resultSize")
                .map(Integer::parseInt)
                .map(size -> Math.min(size, 100))
                .orElse(10);
        Flux<RequestDto> requests = requestService.getRequests(active, sortBy, resultSize).log();
        return ServerResponse.ok()
                .body(requests, RequestDto.class);
    }

    private Mono<ServerResponse> convert(Mono<Request> requestMono) {
        return ServerResponse.created(URI.create("edit/me"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestMono, Request.class);
    }

    private Mono<RequestDto> convert(ServerRequest serverRequest) {
        Mono<RequestDto> requestDtoMono = serverRequest.bodyToMono(RequestDto.class);
        return requestDtoMono;
    }


}
