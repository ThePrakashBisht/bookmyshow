package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.ShowSeatResponse;
import com.bookmyshow.eventservice.service.ShowSeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final ShowSeatService showSeatService;

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getBookingDetails(
            @PathVariable String bookingId) {
        log.info("Get booking details: {}", bookingId);
        List<ShowSeatResponse> seats = showSeatService.getBookingDetails(bookingId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Cancel booking request: {} by user: {}", bookingId, userId);
        showSeatService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully"));
    }
}