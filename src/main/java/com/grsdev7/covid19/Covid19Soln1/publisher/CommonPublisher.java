package com.grsdev7.covid19.Covid19Soln1.publisher;

import com.grsdev7.covid19.Covid19Soln1.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommonPublisher {
    public static final int DEFAULT_RESULT_SIZE = 100;
    private final List<CustomRepository> repositories;

    public <T> Flux<T> getFlux(Example<T> example, Sort sortBy, Integer resultSize) throws Exception {
        CustomRepository repository = getRepository(example.getProbeType());
        Flux<T> dbFlux = repository.findAll(example, sortBy);
        Integer limit = ofNullable(resultSize)
                .orElse(DEFAULT_RESULT_SIZE);
        return dbFlux.limitRequest(limit);
    }

    private <T> CustomRepository getRepository(T type) {
        return repositories.stream()
                .filter(repo -> repo.supports(type.getClass()))
                .findAny().orElseThrow();
    }
}
