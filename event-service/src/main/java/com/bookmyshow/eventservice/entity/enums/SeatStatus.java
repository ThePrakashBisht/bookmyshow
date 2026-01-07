package com.bookmyshow.eventservice.entity.enums;

public enum SeatStatus {
    AVAILABLE,      // Can be booked
    LOCKED,         // Temporarily held (user is in booking process)
    BOOKED,         // Confirmed booking
    BLOCKED         // Blocked by admin (maintenance, etc.)
}