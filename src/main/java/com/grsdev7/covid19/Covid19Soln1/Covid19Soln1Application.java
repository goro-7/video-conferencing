package com.grsdev7.covid19.Covid19Soln1;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableScheduling
public class Covid19Soln1Application {

    public static void main(String[] args) {
        SpringApplication.run(Covid19Soln1Application.class, args);
    }

    @Bean
    public List<Subscriber<String>> subscriberList(){
        return new ArrayList<>(100);
    }
}
