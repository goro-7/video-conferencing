package com.grsdev7.videoconf.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class User {
    private String id;
    private String ipAddress;
}
