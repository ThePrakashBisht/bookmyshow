package com.bookmyshow.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemResponse {

    private Long id;
    private Long showSeatId;
    private Long seatId;
    private String seatLabel;
    private String seatRow;
    private Integer seatNumber;
    private String seatCategory;
    private Double price;
}