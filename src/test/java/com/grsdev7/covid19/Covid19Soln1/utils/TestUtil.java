package com.grsdev7.covid19.Covid19Soln1.utils;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Item;
import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import com.grsdev7.covid19.Covid19Soln1.domain.request.Requestor;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class TestUtil {
    public static Set<Item> buildItemSet(int size) {

        Item item = Item.builder()
                .name("some item")
                .quantity(20)
                .build();
        return
                IntStream.rangeClosed(1, size)
                        .mapToObj(index -> item)
                        .collect(toSet());
    }

    public static List<Request> buildRequestList(int size) {
        return
                IntStream.rangeClosed(1, size)
                        .mapToObj(num -> buildRequest())
                        .collect(toList());
    }

    public static Request buildRequest() {
        return Request.builder()
                .requestor(Requestor.builder().name("john").city("ber").email("test@yo.com").build())
                .active(true)
                .items(buildItemSet(5))
                .build();
    }

    public static List<Request> buildRequestWithIdList(int size) {
        return
                buildRequestList(size).stream()
                        .map(request -> request.withId(UUID.randomUUID().toString()))
                        .collect(toList());
    }
}
