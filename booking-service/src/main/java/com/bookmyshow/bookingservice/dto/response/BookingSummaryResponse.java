package com.bookmyshow.bookingservice.dto.response;

import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryResponse {

    private Long id;
    private String bookingNumber;
    private BookingStatus status;
    private String eventTitle;
    private String venueName;
    private LocalDateTime showTime;
    private Integer totalSeats;
    private Double totalAmount;
    private LocalDateTime createdAt;
}