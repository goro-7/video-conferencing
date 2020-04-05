package com.grsdev7.covid19.Covid19Soln1.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;

@Value
@With
@Builder
public class CommonQueryDto {
    private Boolean active;
    private Instant createdAfter;
    private Integer resultSize;
}
