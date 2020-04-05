package com.grsdev7.covid19.Covid19Soln1.router;

import com.grsdev7.covid19.Covid19Soln1.handler.RequestHandler;
import com.grsdev7.covid19.Covid19Soln1.handler.RootPathRequestHandler;
import com.grsdev7.covid19.Covid19Soln1.stream.ServerSentStreamHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@RequiredArgsConstructor
public class RouterFunctionsConfig {

    private final RootPathRequestHandler rootPathRequestHandler;
    private final RequestHandler requestHandler;
    private final ServerSentStreamHandler serverSentStreamHandler;

    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/static/index.html") final Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).syncBody(indexHtml));
    }

    @Bean
    public RouterFunction<ServerResponse> requestsRouter() {
        return route(POST("/requests"), requestHandler::saveNewRequest)
                .andRoute(GET("/requests"), requestHandler::getRequests);
    }

    @Bean
    public RouterFunction<ServerResponse> sseRouter() {
        return route(GET("/sse"), serverSentStreamHandler::getRequestSSE);
    }
}
