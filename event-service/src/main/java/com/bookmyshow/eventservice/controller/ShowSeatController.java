package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.request.SeatLockRequest;
import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.SeatLockResponse;
import com.bookmyshow.eventservice.dto.response.ShowSeatLayoutResponse;
import com.bookmyshow.eventservice.dto.response.ShowSeatResponse;
import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import com.bookmyshow.eventservice.service.ShowSeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows/{showId}/seats")
@RequiredArgsConstructor
@Slf4j
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    @GetMapping
    public ResponseEntity<ApiResponse<ShowSeatLayoutResponse>> getSeatLayout(
            @PathVariable Long showId) {
        log.info("Get seat layout for show: {}", showId);
        ShowSeatLayoutResponse layout = showSeatService.getSeatLayout(showId);
        return ResponseEntity.ok(ApiResponse.success(layout));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getAvailableSeats(
            @PathVariable Long showId) {
        log.info("Get available seats for show: {}", showId);
        List<ShowSeatResponse> seats = showSeatService.getAvailableSeats(showId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getSeatsByCategory(
            @PathVariable Long showId,
            @PathVariable SeatCategory category) {
        log.info("Get seats by category {} for show: {}", category, showId);
        List<ShowSeatResponse> seats = showSeatService.getSeatsByCategory(showId, category);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeats(
            @PathVariable Long showId,
            @Valid @RequestBody SeatLockRequest request) {
        log.info("Lock seats request for show: {}, user: {}", showId, request.getUserId());
        // Ensure showId from path matches request
        request.setShowId(showId);
        SeatLockResponse response = showSeatService.lockSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats locked successfully", response));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @PathVariable Long showId,
            @RequestBody List<Long> showSeatIds,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Release seats for show: {}, user: {}", showId, userId);
        showSeatService.releaseSeats(showId, showSeatIds, userId);
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> confirmBooking(
            @PathVariable Long showId,
            @RequestBody List<Long> showSeatIds,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Booking-Id") String bookingId) {
        log.info("Confirm booking for show: {}, user: {}, booking: {}",
                showId, userId, bookingId);
        List<ShowSeatResponse> response = showSeatService.confirmBooking(
                showId, showSeatIds, userId, bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed", response));
    }
}