package com.bookmyshow.bookingservice.entity.enums;

public enum BookingStatus {
    PENDING,        // Seats locked, awaiting payment
    CONFIRMED,      // Payment successful, booking confirmed
    CANCELLED,      // Cancelled by user or system
    EXPIRED,        // Lock expired before payment
    FAILED          // Payment failed
}