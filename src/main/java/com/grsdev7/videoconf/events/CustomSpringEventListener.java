package com.grsdev7.videoconf.events;

import org.springframework.stereotype.Component;

@Component
public class CustomSpringEventListener {
    //@Override
    public void onApplicationEvent(CustomSpringEvent event) {
        System.out.println("Received spring custom event - " + event.getMessage());
    }
}
