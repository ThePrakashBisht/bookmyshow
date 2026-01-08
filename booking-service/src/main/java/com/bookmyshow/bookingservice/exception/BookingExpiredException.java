package com.bookmyshow.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class BookingExpiredException extends RuntimeException {

    public BookingExpiredException(String bookingNumber) {
        super(String.format("Booking %s has expired. Please start a new booking.", bookingNumber));
    }
}