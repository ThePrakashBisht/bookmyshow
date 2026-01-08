package com.bookmyshow.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatLockException extends RuntimeException {

    public SeatLockException(String message) {
        super(message);
    }

    public SeatLockException(String seatLabel, String reason) {
        super(String.format("Cannot lock seat %s: %s", seatLabel, reason));
    }
}