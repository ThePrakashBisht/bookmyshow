package com.bookmyshow.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowInfo {

    private Long id;
    private LocalDateTime showTime;
    private LocalDateTime endTime;
    private String status;
    private Double basePrice;
    private Integer availableSeats;
    private Integer totalSeats;

    // Event info
    private Long eventId;
    private String eventTitle;
    private Integer eventDurationMinutes;

    // Venue info
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private String cityName;
}