package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSummaryResponse {

    private Long id;
    private LocalDateTime showTime;
    private LocalTime time;          // Just the time part for display
    private ShowStatus status;
    private Double basePrice;
    private Integer availableSeats;
    private Integer totalSeats;
    private String venueName;
    private Long venueId;
}