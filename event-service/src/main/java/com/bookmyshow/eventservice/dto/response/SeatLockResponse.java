package com.bookmyshow.eventservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {

    private boolean success;
    private String message;
    private Long showId;
    private Long userId;
    private List<Long> lockedSeatIds;
    private List<String> lockedSeatLabels;  // ["A1", "A2", "A3"]
    private LocalDateTime lockExpiresAt;
    private int lockDurationMinutes;
    private Double totalPrice;
    private int seatCount;
}