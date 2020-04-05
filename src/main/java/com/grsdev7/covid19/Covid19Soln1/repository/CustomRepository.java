package com.grsdev7.covid19.Covid19Soln1.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomRepository<T, ID> extends ReactiveMongoRepository<T, ID> {

    default boolean supports(Class<?> type) {
        throw new IllegalArgumentException("Child interface should override this");
    }
}
