package com.bookmyshow.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatInfo {

    private Long id;            // ShowSeat ID
    private Long seatId;        // Physical Seat ID
    private Long showId;
    private String seatRow;
    private Integer seatNumber;
    private String seatLabel;
    private String category;
    private String status;
    private Double price;
    private boolean available;
}