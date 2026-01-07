package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.EventRequest;
import com.bookmyshow.eventservice.dto.response.EventResponse;
import com.bookmyshow.eventservice.dto.response.EventSummaryResponse;
import com.bookmyshow.eventservice.dto.response.PagedResponse;
import com.bookmyshow.eventservice.entity.Event;
import com.bookmyshow.eventservice.entity.enums.EventStatus;
import com.bookmyshow.eventservice.entity.enums.EventType;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.repository.EventRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        log.info("Creating event: {}", request.getTitle());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .status(EventStatus.UPCOMING)
                .durationMinutes(request.getDurationMinutes())
                .language(request.getLanguage())
                .genre(request.getGenre())
                .certificate(request.getCertificate())
                .releaseDate(request.getReleaseDate())
                .endDate(request.getEndDate())
                .posterUrl(request.getPosterUrl())
                .bannerUrl(request.getBannerUrl())
                .trailerUrl(request.getTrailerUrl())
                .cast(request.getCast())
                .crew(request.getCrew())
                .rating(0.0)
                .totalVotes(0L)
                .active(true)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with ID: {}", savedEvent.getId());

        return entityMapper.toEventResponse(savedEvent);
    }

    public EventResponse getEventById(Long id) {
        Event event = findEventById(id);
        return entityMapper.toEventResponse(event);
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return entityMapper.toEventResponseList(events);
    }

    public List<EventSummaryResponse> getEventsByType(EventType type) {
        List<Event> events = eventRepository.findByEventTypeAndActiveTrue(type);
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> searchEvents(String query) {
        List<Event> events = eventRepository.searchByTitle(query);
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> getEventsByLanguage(String language) {
        List<Event> events = eventRepository.findByLanguageIgnoreCaseAndActiveTrue(language);
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> getEventsByGenre(String genre) {
        List<Event> events = eventRepository.findByGenreIgnoreCaseAndActiveTrue(genre);
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> getCurrentEvents() {
        List<Event> events = eventRepository.findCurrentEvents(LocalDate.now());
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> getUpcomingEvents() {
        List<Event> events = eventRepository.findUpcomingEvents(LocalDate.now());
        return entityMapper.toEventSummaryResponseList(events);
    }

    public List<EventSummaryResponse> getEventsInCity(Long cityId) {
        List<Event> events = eventRepository.findEventsWithShowsInCity(cityId);
        return entityMapper.toEventSummaryResponseList(events);
    }

    public PagedResponse<EventSummaryResponse> filterEvents(
            EventType eventType,
            String language,
            String genre,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Event> eventPage = eventRepository.searchEvents(eventType, language, genre, pageable);

        List<EventSummaryResponse> content = entityMapper.toEventSummaryResponseList(
                eventPage.getContent());

        return PagedResponse.of(
                content,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        log.info("Updating event with ID: {}", id);

        Event event = findEventById(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setDurationMinutes(request.getDurationMinutes());
        event.setLanguage(request.getLanguage());
        event.setGenre(request.getGenre());
        event.setCertificate(request.getCertificate());
        event.setReleaseDate(request.getReleaseDate());
        event.setEndDate(request.getEndDate());
        event.setPosterUrl(request.getPosterUrl());
        event.setBannerUrl(request.getBannerUrl());
        event.setTrailerUrl(request.getTrailerUrl());

        if (request.getCast() != null) {
            event.setCast(request.getCast());
        }
        if (request.getCrew() != null) {
            event.setCrew(request.getCrew());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated: {}", updatedEvent.getTitle());

        return entityMapper.toEventResponse(updatedEvent);
    }

    @Transactional
    public EventResponse updateEventStatus(Long id, EventStatus status) {
        log.info("Updating event status: {} -> {}", id, status);

        Event event = findEventById(id);
        event.setStatus(status);

        Event updatedEvent = eventRepository.save(event);
        return entityMapper.toEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event with ID: {}", id);

        Event event = findEventById(id);
        event.setActive(false);  // Soft delete
        eventRepository.save(event);

        log.info("Event soft deleted: {}", event.getTitle());
    }

    @Transactional
    public EventResponse toggleActiveStatus(Long id) {
        log.info("Toggling active status for event: {}", id);

        Event event = findEventById(id);
        event.setActive(!event.isActive());
        Event updatedEvent = eventRepository.save(event);

        log.info("Event {} active status changed to: {}", id, updatedEvent.isActive());
        return entityMapper.toEventResponse(updatedEvent);
    }

    // ============ Internal Methods ============

    public Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }
}