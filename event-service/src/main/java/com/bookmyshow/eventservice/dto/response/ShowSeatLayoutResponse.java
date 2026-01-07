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
public class ShowSeatLayoutResponse {

    private Long showId;
    private String eventTitle;
    private Long venueId;
    private String venueName;
    private int totalSeats;
    private int availableSeats;
    private int bookedSeats;
    private int lockedSeats;

    // Seats organized by row with their current status
    private Map<String, List<ShowSeatResponse>> seatsByRow;

    // Price per category for this show
    private Map<SeatCategory, Double> priceByCategory;

    // Available seat count per category
    private Map<SeatCategory, Integer> availableByCategory;
}