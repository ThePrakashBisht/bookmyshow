package com.bookmyshow.eventservice.dto.response;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {

    private Long id;
    private Long seatId;
    private Long showId;
    private String seatRow;
    private Integer seatNumber;
    private String seatLabel;
    private SeatCategory category;
    private SeatStatus status;
    private Double price;
    private boolean available;
}