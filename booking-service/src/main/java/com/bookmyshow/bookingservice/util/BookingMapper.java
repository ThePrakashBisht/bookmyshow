package com.bookmyshow.bookingservice.util;

import com.bookmyshow.bookingservice.dto.response.BookingItemResponse;
import com.bookmyshow.bookingservice.dto.response.BookingResponse;
import com.bookmyshow.bookingservice.dto.response.BookingSummaryResponse;
import com.bookmyshow.bookingservice.entity.Booking;
import com.bookmyshow.bookingservice.entity.BookingItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public BookingResponse toBookingResponse(Booking booking) {
        List<BookingItemResponse> seatResponses = booking.getBookingItems().stream()
                .map(this::toBookingItemResponse)
                .collect(Collectors.toList());

        // Calculate remaining seconds for payment
        long remainingSeconds = 0;
        if (booking.getLockExpiresAt() != null &&
                booking.getLockExpiresAt().isAfter(LocalDateTime.now())) {
            remainingSeconds = ChronoUnit.SECONDS.between(
                    LocalDateTime.now(),
                    booking.getLockExpiresAt()
            );
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .userId(booking.getUserId())
                .status(booking.getStatus())
                .showId(booking.getShowId())
                .eventId(booking.getEventId())
                .eventTitle(booking.getEventTitle())
                .venueId(booking.getVenueId())
                .venueName(booking.getVenueName())
                .showTime(booking.getShowTime())
                .totalSeats(booking.getTotalSeats())
                .seats(seatResponses)
                .subtotal(booking.getSubtotal())
                .convenienceFee(booking.getConvenienceFee())
                .taxAmount(booking.getTaxAmount())
                .totalAmount(booking.getTotalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .paymentTransactionId(booking.getPaymentTransactionId())
                .lockExpiresAt(booking.getLockExpiresAt())
                .remainingSeconds(remainingSeconds)
                .createdAt(booking.getCreatedAt())
                .userEmail(booking.getUserEmail())
                .userPhone(booking.getUserPhone())
                .build();
    }

    public BookingItemResponse toBookingItemResponse(BookingItem item) {
        return BookingItemResponse.builder()
                .id(item.getId())
                .showSeatId(item.getShowSeatId())
                .seatId(item.getSeatId())
                .seatLabel(item.getSeatLabel())
                .seatRow(item.getSeatRow())
                .seatNumber(item.getSeatNumber())
                .seatCategory(item.getSeatCategory())
                .price(item.getPrice())
                .build();
    }

    public BookingSummaryResponse toBookingSummaryResponse(Booking booking) {
        return BookingSummaryResponse.builder()
                .id(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .status(booking.getStatus())
                .eventTitle(booking.getEventTitle())
                .venueName(booking.getVenueName())
                .showTime(booking.getShowTime())
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public List<BookingResponse> toBookingResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    public List<BookingSummaryResponse> toBookingSummaryResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingSummaryResponse)
                .collect(Collectors.toList());
    }
}