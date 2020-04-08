package com.grsdev7.videoconf.utils;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}

class MyOptional{

}