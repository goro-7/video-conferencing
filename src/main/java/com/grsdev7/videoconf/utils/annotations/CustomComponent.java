package com.grsdev7.videoconf.utils.annotations;

import org.checkerframework.framework.qual.InheritedAnnotation;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@InheritedAnnotation
public @interface CustomComponent {
}
