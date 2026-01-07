package com.bookmyshow.eventservice.dto.request;

import com.bookmyshow.eventservice.entity.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchRequest {

    private String title;
    private EventType eventType;
    private String language;
    private String genre;
    private Long cityId;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "releaseDate";

    @Builder.Default
    private String sortDirection = "DESC";
}