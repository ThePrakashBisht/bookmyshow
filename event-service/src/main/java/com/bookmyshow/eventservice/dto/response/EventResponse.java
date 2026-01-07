package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.EventStatus;
import com.bookmyshow.eventservice.entity.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private EventType eventType;
    private EventStatus status;
    private Integer durationMinutes;
    private String language;
    private String genre;
    private String certificate;
    private LocalDate releaseDate;
    private LocalDate endDate;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private Double rating;
    private Long totalVotes;
    private List<String> cast;
    private List<String> crew;
    private boolean active;
    private LocalDateTime createdAt;
}