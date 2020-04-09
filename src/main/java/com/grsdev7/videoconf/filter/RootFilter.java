package com.grsdev7.videoconf.filter;

import com.grsdev7.videoconf.domain.User;
import com.grsdev7.videoconf.repository.IpAddressRepository;
import com.grsdev7.videoconf.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Slf4j
@Component
public class RootFilter implements WebFilter {
    public final static String COOKIE = "videoConfCookie";
    public static final String USER_ID = "userId";
    private final UserRepository userRepository;
    private final IpAddressRepository ipAddressRepository;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // todo remove when javascript user id approach works
        /*
         ServerHttpRequest request = exchange.getRequest();
        if (request.getPath().pathWithinApplication().value().trim().equalsIgnoreCase("/")) {
            String userId = getIpAddress(request)
                    .map(ip -> getUserIdFromIpOrCreate(ip).toString())
                    .orElseThrow(() -> new RuntimeException("Unable to get ip address of client"));
            // add user id to request
            request.mutate().headers(httpHeaders -> {
                httpHeaders.set(USER_ID, userId);
            });
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().set(USER_ID, userId);
            exchange = exchange.mutate()
                    .request(request)
                    .response(response)
                    .build();
        }*/
        return chain.filter(exchange);
    }


    /*private User createNewUser(String ipAddress) {
        User user = User.builder()
                .ipAddress(ipAddress)
                .build();
        User saved = userRepository.save(user);
        ipAddressRepository.add(ipAddress, saved.getId());
        log.info("New user created : {} for Ip : {}", saved, ipAddress);
        return saved;
    }*/

    private Optional<String> getIpAddress(ServerHttpRequest request) {
        return ofNullable(request.getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress);
    }

/*    private String getUserIdFromIpOrCreate(@NonNull String ipAddress) {
        String userId =
                ipAddressRepository.findUserIdByIp(ipAddress)
                        .orElseGet(() -> createNewUser(ipAddress).getId());
        return userId;
    }*/
}
