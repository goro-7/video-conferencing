package com.grsdev7.videoconf.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.web.reactive.socket.WebSocketSession;

@Value
@Builder
@With
public class User {
    private Integer id;
    private WebSocketSession session;
    private String ipAddress;
}
