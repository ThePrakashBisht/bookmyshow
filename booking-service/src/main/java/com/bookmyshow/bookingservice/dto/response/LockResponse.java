package com.bookmyshow.bookingservice.dto.response;

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
public class LockResponse {

    private boolean success;
    private String message;
    private String lockId;
    private Long showId;
    private List<Long> lockedSeatIds;
    private List<String> lockedSeatLabels;
    private LocalDateTime lockExpiresAt;
    private int lockDurationSeconds;
    private Double totalPrice;
}