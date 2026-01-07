package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.request.BulkSeatRequest;
import com.bookmyshow.eventservice.dto.request.SeatRequest;
import com.bookmyshow.eventservice.dto.request.VenueRequest;
import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.SeatLayoutResponse;
import com.bookmyshow.eventservice.dto.response.SeatResponse;
import com.bookmyshow.eventservice.dto.response.VenueResponse;
import com.bookmyshow.eventservice.service.SeatService;
import com.bookmyshow.eventservice.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Slf4j
public class VenueController {

    private final VenueService venueService;
    private final SeatService seatService;

    // ============ VENUE ENDPOINTS ============

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(
            @Valid @RequestBody VenueRequest request) {
        log.info("Create venue request: {}", request.getName());
        VenueResponse response = venueService.createVenue(request);
        return new ResponseEntity<>(
                ApiResponse.success("Venue created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        log.info("Get all venues request");
        List<VenueResponse> venues = venueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.success(venues));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable Long id) {
        log.info("Get venue by ID: {}", id);
        VenueResponse response = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getVenuesByCity(
            @PathVariable Long cityId) {
        log.info("Get venues by city: {}", cityId);
        List<VenueResponse> venues = venueService.getVenuesByCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(venues));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {
        log.info("Update venue: {}", id);
        VenueResponse response = venueService.updateVenue(id, request);
        return ResponseEntity.ok(ApiResponse.success("Venue updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id) {
        log.info("Delete venue: {}", id);
        venueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.success("Venue deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<VenueResponse>> toggleVenueStatus(@PathVariable Long id) {
        log.info("Toggle venue status: {}", id);
        VenueResponse response = venueService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Venue status updated", response));
    }

    // ============ SEAT ENDPOINTS ============

    @PostMapping("/{venueId}/seats")
    public ResponseEntity<ApiResponse<SeatResponse>> addSeat(
            @PathVariable Long venueId,
            @Valid @RequestBody SeatRequest request) {
        log.info("Add seat to venue: {}", venueId);
        SeatResponse response = seatService.createSeat(venueId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Seat added successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{venueId}/seats/bulk")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> addBulkSeats(
            @PathVariable Long venueId,
            @Valid @RequestBody BulkSeatRequest request) {
        log.info("Add bulk seats to venue: {}", venueId);
        request.setVenueId(venueId);  // Ensure venue ID matches path variable
        List<SeatResponse> response = seatService.createBulkSeats(request);
        return new ResponseEntity<>(
                ApiResponse.success(response.size() + " seats added successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{venueId}/seats")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getVenueSeats(
            @PathVariable Long venueId) {
        log.info("Get seats for venue: {}", venueId);
        List<SeatResponse> seats = seatService.getSeatsByVenue(venueId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @GetMapping("/{venueId}/seats/layout")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> getSeatLayout(
            @PathVariable Long venueId) {
        log.info("Get seat layout for venue: {}", venueId);
        SeatLayoutResponse layout = seatService.getSeatLayout(venueId);
        return ResponseEntity.ok(ApiResponse.success(layout));
    }

    @DeleteMapping("/{venueId}/seats/{seatId}")
    public ResponseEntity<ApiResponse<Void>> deleteSeat(
            @PathVariable Long venueId,
            @PathVariable Long seatId) {
        log.info("Delete seat {} from venue {}", seatId, venueId);
        seatService.deleteSeat(venueId, seatId);
        return ResponseEntity.ok(ApiResponse.success("Seat deleted successfully"));
    }
}