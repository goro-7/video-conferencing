package com.grsdev7.covid19.Covid19Soln1.handler;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class RootPathRequestHandler {


    public Mono<ServerResponse> handle(ServerRequest serverRequest) {
        return ServerResponse.permanentRedirect(URI.create("index.html"))
                .build();
    }
}
