package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutResponse {

    private Long venueId;
    private String venueName;
    private Integer totalSeats;

    // Seats organized by row: {"A": [seat1, seat2...], "B": [...]}
    private Map<String, List<SeatResponse>> seatsByRow;

    // Count of seats per category
    private Map<SeatCategory, Integer> seatCountByCategory;

    // Base price per category
    private Map<SeatCategory, Double> priceByCategory;
}