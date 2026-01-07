package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {

    private Long id;
    private LocalDateTime showTime;
    private LocalDateTime endTime;
    private ShowStatus status;
    private Double basePrice;
    private Integer availableSeats;
    private Integer totalSeats;

    // Event info
    private EventSummaryResponse event;

    // Venue info
    private VenueSummaryResponse venue;
}