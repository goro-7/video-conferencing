package com.grsdev7.videoconf.utils;

import com.grsdev7.videoconf.utils.annotations.CustomComponent;
import org.slf4j.Logger;

import java.util.Optional;

@CustomComponent
public abstract class CustomBean {
    protected final Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    protected <T> Optional<T> of(T value) {
        return Optional.ofNullable(value);
    }
}
