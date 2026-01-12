package com.bookmyshow.bookingservice.controller;

import com.bookmyshow.bookingservice.dto.request.CancelBookingRequest;
import com.bookmyshow.bookingservice.dto.request.ConfirmPaymentRequest;
import com.bookmyshow.bookingservice.dto.request.InitiateBookingRequest;
import com.bookmyshow.bookingservice.dto.response.ApiResponse;
import com.bookmyshow.bookingservice.dto.response.BookingResponse;
import com.bookmyshow.bookingservice.dto.response.BookingSummaryResponse;
import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import com.bookmyshow.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Step 1: Initiate a booking (locks seats)
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<BookingResponse>> initiateBooking(
            @Valid @RequestBody InitiateBookingRequest request) {

        log.info("Initiate booking request - User: {}, Show: {}, Seats: {}",
                request.getUserId(), request.getShowId(), request.getShowSeatIds().size());

        BookingResponse response = bookingService.initiateBooking(request);

        return new ResponseEntity<>(
                ApiResponse.success("Booking initiated. Complete payment within 5 minutes.", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Step 2: Confirm payment and complete booking
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request) {

        log.info("Confirm payment request - Booking: {}", request.getBookingNumber());

        BookingResponse response = bookingService.confirmPayment(request);

        return ResponseEntity.ok(
                ApiResponse.success("Payment successful! Booking confirmed.", response)
        );
    }

    /**
     * Cancel a booking
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @Valid @RequestBody CancelBookingRequest request) {

        log.info("Cancel booking request - Booking: {}", request.getBookingNumber());

        BookingResponse response = bookingService.cancelBooking(request);

        return ResponseEntity.ok(
                ApiResponse.success("Booking cancelled successfully.", response)
        );
    }

    /**
     * Get booking by booking number
     */
    @GetMapping("/{bookingNumber}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable String bookingNumber) {

        log.info("Get booking request - Booking: {}", bookingNumber);

        BookingResponse response = bookingService.getBookingByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long bookingId) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Get all bookings for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingSummaryResponse>>> getUserBookings(
            @PathVariable Long userId) {

        log.info("Get user bookings request - User: {}", userId);

        List<BookingSummaryResponse> response = bookingService.getUserBookings(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user bookings by status
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<BookingSummaryResponse>>> getUserBookingsByStatus(
            @PathVariable Long userId,
            @PathVariable BookingStatus status) {

        log.info("Get user bookings by status - User: {}, Status: {}", userId, status);

        List<BookingSummaryResponse> response = bookingService.getUserBookingsByStatus(userId, status);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Booking Service is running!"));
    }
}