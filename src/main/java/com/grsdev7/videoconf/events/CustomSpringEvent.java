package com.grsdev7.videoconf.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.buffer.DataBuffer;

public class CustomSpringEvent extends ApplicationEvent {
    private DataBuffer message;

    public CustomSpringEvent(Object source, DataBuffer message) {
        super(source);
        this.message = message;
    }
    public DataBuffer getMessage() {
        return message;
    }
}

