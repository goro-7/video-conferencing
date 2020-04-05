package com.grsdev7.covid19.Covid19Soln1.service;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.dto.ItemDto;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestDto;
import com.grsdev7.covid19.Covid19Soln1.dto.RequestorDto;
import com.grsdev7.covid19.Covid19Soln1.repository.RequestRepository;
import io.micrometer.core.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    public Flux<RequestDto> getRequests(Example<Request> probe, Sort sortBy, @Nullable Integer resultLimit) {
        Flux<RequestDto> requestDtoFlux = requestRepository.findAll(probe, sortBy)
                .map(RequestDto::fromRequest)
                .delayElements(Duration.ofSeconds(1))
                .log();
        return
                ofNullable(resultLimit)
                        .map(size -> requestDtoFlux.limitRequest(size))
                        .orElse(requestDtoFlux);
    }

    public Flux<RequestDto> getAllByActiveAndCreatedOnGreaterThan(boolean active, Instant time) {
        return requestRepository.findAllByActiveAndCreatedOnGreaterThanOrderByCreatedOnDesc(active, time)
                .map(RequestDto::fromRequest)
                ;
    }

    public Flux<RequestDto> getRequests(boolean active, String sortBy, Integer resultSize) {
        Example<Request> probe = Example.of(Request.builder().active(true).build());
        Sort sortByObject = Sort.by(sortBy).descending();
        return getRequests(probe, sortByObject, resultSize);
    }

    private BigInteger getShortId(BigInteger id) {
        String idString = id.toString();
        int length = idString.length();
        String shortIdString = new StringBuilder(idString).reverse()
                .toString()
                .substring(0, Math.min(length, 5));
        return new BigInteger(shortIdString);
    }

    private Example<Request> getRequestProbe() {
        Request request = Request.builder()
                .active(true)
                .build();
        return Example.of(request);
    }

    public Mono<Request> saveRequest(Mono<RequestDto> requestDto) {
        Mono<Request> request = requestDto.map(this::convertToRequest);
        log.info("Converted to request : {}", request);
        Mono<Request> savedRequestMono = request.flatMap(data -> requestRepository.save(data));
        log.info("Saved to DB : {}", savedRequestMono);
        return savedRequestMono;
    }

    private Request convertToRequest(RequestDto requestDto) {
        return Request.builder()
                .items(ItemDto.convertToItems(getItemsDtos(requestDto)))
                .requestor(ofNullable(requestDto.getRequestor()).map(RequestorDto::toUser).orElse(null))
                .active(true)
                .build();
    }

    private Set<ItemDto> getItemsDtos(RequestDto requestDto) {
        return ofNullable(requestDto.getItemDtos())
                .orElse(Set.of());
    }


}
