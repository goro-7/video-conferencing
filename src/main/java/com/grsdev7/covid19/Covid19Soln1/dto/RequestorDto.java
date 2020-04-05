package com.grsdev7.covid19.Covid19Soln1.dto;

import com.grsdev7.covid19.Covid19Soln1.domain.request.Requestor;
import lombok.*;

@Data
@Builder
@With
public class RequestorDto {
    private String email;
    private String name;
    private String city;

    public static RequestorDto from(Requestor requestor) {
        return RequestorDto.builder()
                .name(requestor.getName())
                .email(requestor.getEmail())
                .city(requestor.getCity())
                .build();
    }


    public static Requestor toUser(@NonNull RequestorDto requestorDto) {
        return Requestor.builder()
                .name(requestorDto.getName())
                .email(requestorDto.getEmail())
                .city(requestorDto.getCity())
                .build();
    }
}
