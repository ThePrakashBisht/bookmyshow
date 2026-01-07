package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.request.EventRequest;
import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.EventResponse;
import com.bookmyshow.eventservice.dto.response.EventSummaryResponse;
import com.bookmyshow.eventservice.dto.response.PagedResponse;
import com.bookmyshow.eventservice.entity.enums.EventStatus;
import com.bookmyshow.eventservice.entity.enums.EventType;
import com.bookmyshow.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request) {
        log.info("Create event request: {}", request.getTitle());
        EventResponse response = eventService.createEvent(request);
        return new ResponseEntity<>(
                ApiResponse.success("Event created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        log.info("Get all events request");
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        log.info("Get event by ID: {}", id);
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getEventsByType(
            @PathVariable EventType type) {
        log.info("Get events by type: {}", type);
        List<EventSummaryResponse> events = eventService.getEventsByType(type);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> searchEvents(
            @RequestParam String query) {
        log.info("Search events: {}", query);
        List<EventSummaryResponse> events = eventService.searchEvents(query);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getEventsByLanguage(
            @PathVariable String language) {
        log.info("Get events by language: {}", language);
        List<EventSummaryResponse> events = eventService.getEventsByLanguage(language);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getEventsByGenre(
            @PathVariable String genre) {
        log.info("Get events by genre: {}", genre);
        List<EventSummaryResponse> events = eventService.getEventsByGenre(genre);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getCurrentEvents() {
        log.info("Get current/now showing events");
        List<EventSummaryResponse> events = eventService.getCurrentEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getUpcomingEvents() {
        log.info("Get upcoming events");
        List<EventSummaryResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getEventsInCity(
            @PathVariable Long cityId) {
        log.info("Get events in city: {}", cityId);
        List<EventSummaryResponse> events = eventService.getEventsInCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponse<EventSummaryResponse>>> filterEvents(
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Filter events - type: {}, language: {}, genre: {}", type, language, genre);
        PagedResponse<EventSummaryResponse> events = eventService.filterEvents(
                type, language, genre, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        log.info("Update event: {}", id);
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventResponse>> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        log.info("Update event status: {} -> {}", id, status);
        EventResponse response = eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Event status updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        log.info("Delete event: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<EventResponse>> toggleEventActiveStatus(@PathVariable Long id) {
        log.info("Toggle event active status: {}", id);
        EventResponse response = eventService.toggleActiveStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Event status updated", response));
    }
}