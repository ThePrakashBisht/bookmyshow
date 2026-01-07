package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryResponse {

    private Long id;
    private String title;
    private EventType eventType;
    private String language;
    private String genre;
    private String certificate;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
    private Double rating;
    private Long totalVotes;
}