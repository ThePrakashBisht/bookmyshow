package com.bookmyshow.eventservice.entity.enums;

public enum ShowStatus {
    SCHEDULED,      // Show is scheduled
    OPEN,           // Booking is open
    FAST_FILLING,   // Most seats booked
    SOLD_OUT,       // All seats booked
    CANCELLED,      // Show cancelled
    COMPLETED       // Show has ended
}