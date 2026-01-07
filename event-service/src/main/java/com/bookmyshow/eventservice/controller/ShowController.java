package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.request.ShowRequest;
import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.ShowResponse;
import com.bookmyshow.eventservice.dto.response.ShowSummaryResponse;
import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import com.bookmyshow.eventservice.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Slf4j
public class ShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @Valid @RequestBody ShowRequest request) {
        log.info("Create show request for event: {} at venue: {}",
                request.getEventId(), request.getVenueId());
        ShowResponse response = showService.createShow(request);
        return new ResponseEntity<>(
                ApiResponse.success("Show created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(@PathVariable Long id) {
        log.info("Get show by ID: {}", id);
        ShowResponse response = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<ShowResponse>>> getShowsByEvent(
            @PathVariable Long eventId) {
        log.info("Get shows for event: {}", eventId);
        List<ShowResponse> shows = showService.getShowsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(shows));
    }

    @GetMapping("/event/{eventId}/city/{cityId}")
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>> getShowsForEventInCity(
            @PathVariable Long eventId,
            @PathVariable Long cityId) {
        log.info("Get shows for event: {} in city: {}", eventId, cityId);
        List<ShowSummaryResponse> shows = showService.getShowsForEventInCity(eventId, cityId);
        return ResponseEntity.ok(ApiResponse.success(shows));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>> getShowsByVenue(
            @PathVariable Long venueId) {
        log.info("Get shows for venue: {}", venueId);
        List<ShowSummaryResponse> shows = showService.getShowsByVenue(venueId);
        return ResponseEntity.ok(ApiResponse.success(shows));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ShowResponse>>> getUpcomingShows() {
        log.info("Get upcoming shows");
        List<ShowResponse> shows = showService.getUpcomingShows();
        return ResponseEntity.ok(ApiResponse.success(shows));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShowStatus(
            @PathVariable Long id,
            @RequestParam ShowStatus status) {
        log.info("Update show status: {} -> {}", id, status);
        ShowResponse response = showService.updateShowStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Show status updated", response));
    }

    @PostMapping("/{id}/open-booking")
    public ResponseEntity<ApiResponse<ShowResponse>> openBooking(@PathVariable Long id) {
        log.info("Open booking for show: {}", id);
        ShowResponse response = showService.openBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking opened", response));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelShow(@PathVariable Long id) {
        log.info("Cancel show: {}", id);
        showService.cancelShow(id);
        return ResponseEntity.ok(ApiResponse.success("Show cancelled successfully"));
    }
}