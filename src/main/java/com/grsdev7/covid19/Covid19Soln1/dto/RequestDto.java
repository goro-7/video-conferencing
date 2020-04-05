package com.grsdev7.covid19.Covid19Soln1.dto;


import com.grsdev7.covid19.Covid19Soln1.domain.request.Request;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@With
public class RequestDto {
    private String id;
    private Set<ItemDto> itemDtos;
    private RequestorDto requestor;
    private Instant createdOn;

    public static RequestDto fromRequest(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .itemDtos(ItemDto.convertToItemDtos(request.getItems()))
                .requestor(RequestorDto.from(request.getRequestor()))
                .createdOn(request.getCreatedOn())
                .build();
    }
}
