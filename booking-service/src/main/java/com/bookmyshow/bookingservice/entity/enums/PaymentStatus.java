package com.bookmyshow.bookingservice.entity.enums;

public enum PaymentStatus {
    PENDING,        // Awaiting payment
    PROCESSING,     // Payment in progress
    COMPLETED,      // Payment successful
    FAILED,         // Payment failed
    REFUNDED        // Payment refunded
}