package com.grsdev7.videoconf.domain;

import com.grsdev7.videoconf.service.MediaProcessor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketSession;


@Value
@Builder
@With
public class User {
    private String id;
    private WebSocketSession session;
    private MediaProcessor<DataBuffer> processor;
    private String ipAddress;
}
