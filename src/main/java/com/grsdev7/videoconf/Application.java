package com.grsdev7.videoconf;

import com.grsdev7.videoconf.utils.annotations.CustomComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@RequiredArgsConstructor
@EnableCaching
@ComponentScan(basePackages = "com.grsdev7", includeFilters = @ComponentScan.Filter(CustomComponent.class))
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
