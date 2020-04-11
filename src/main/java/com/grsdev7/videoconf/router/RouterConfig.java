package com.grsdev7.videoconf.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> rootRouter(@Value("classpath:/static/index.html") final Resource indexHtml) {
        return route(GET("/"), request -> ok()
                .contentType(TEXT_HTML)
                .syncBody(indexHtml))
                ;
    }

    @Bean
    public RouterFunction<ServerResponse> staticResourceRouter() {
        return
                RouterFunctions.resources("/**", new ClassPathResource("static/"))
                        .filter(getFilter());
    }

    private HandlerFilterFunction getFilter() {

        Function<ServerResponse, Mono<ServerResponse>> responseProcessor = response -> {
           // log.info("returning response : {}", response);
            return Mono.just(response);
        };
        return HandlerFilterFunction.ofResponseProcessor(responseProcessor);
    }

}
