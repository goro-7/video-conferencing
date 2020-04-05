package com.grsdev7.covid19.Covid19Soln1.domain.request;


import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document
@Value
@Builder
@With
public class Request {
    @Id
    private String id;

    private Requestor requestor;

    private Set<Item> items;

    private Boolean active;

    private String fullFilledBy;

    @CreatedDate
    private Instant createdOn;
}
