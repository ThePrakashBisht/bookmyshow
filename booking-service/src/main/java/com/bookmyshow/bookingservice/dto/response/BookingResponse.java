package com.bookmyshow.bookingservice.dto.response;

import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import com.bookmyshow.bookingservice.entity.enums.PaymentMethod;
import com.bookmyshow.bookingservice.entity.enums.PaymentStatus;
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
public class BookingResponse {

    private Long id;
    private String bookingNumber;
    private Long userId;
    private BookingStatus status;

    // Show details
    private Long showId;
    private Long eventId;
    private String eventTitle;
    private Long venueId;
    private String venueName;
    private LocalDateTime showTime;

    // Seat details
    private Integer totalSeats;
    private List<BookingItemResponse> seats;

    // Pricing
    private Double subtotal;
    private Double convenienceFee;
    private Double taxAmount;
    private Double totalAmount;

    // Payment
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String paymentTransactionId;

    // Timing
    private LocalDateTime lockExpiresAt;
    private Long remainingSeconds;      // Seconds left to complete payment
    private LocalDateTime createdAt;

    // Contact
    private String userEmail;
    private String userPhone;
}