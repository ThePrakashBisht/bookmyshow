package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private String seatRow;
    private Integer seatNumber;
    private String seatLabel;      // "A1", "B12"
    private SeatCategory category;
    private Double basePrice;
    private Long venueId;
}