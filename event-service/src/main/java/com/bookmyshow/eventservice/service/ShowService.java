package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.ShowRequest;
import com.bookmyshow.eventservice.dto.response.ShowResponse;
import com.bookmyshow.eventservice.dto.response.ShowSummaryResponse;
import com.bookmyshow.eventservice.entity.Event;
import com.bookmyshow.eventservice.entity.Seat;
import com.bookmyshow.eventservice.entity.Show;
import com.bookmyshow.eventservice.entity.ShowSeat;
import com.bookmyshow.eventservice.entity.Venue;
import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import com.bookmyshow.eventservice.exception.BadRequestException;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.repository.ShowRepository;
import com.bookmyshow.eventservice.repository.ShowSeatRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final EventService eventService;
    private final VenueService venueService;
    private final SeatService seatService;
    private final EntityMapper entityMapper;

    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        log.info("Creating show for event: {} at venue: {}",
                request.getEventId(), request.getVenueId());

        Event event = eventService.findEventById(request.getEventId());
        Venue venue = venueService.findVenueById(request.getVenueId());

        // Get seats for the venue
        List<Seat> seats = seatService.findSeatsByVenueId(request.getVenueId());
        if (seats.isEmpty()) {
            throw new BadRequestException("Venue has no seats configured. Please add seats first.");
        }

        // Calculate end time
        LocalDateTime endTime = request.getShowTime()
                .plusMinutes(event.getDurationMinutes())
                .plusMinutes(15);  // 15 min buffer for cleaning/ads

        // Check for conflicting shows
        if (showRepository.existsConflictingShow(venue.getId(), request.getShowTime(), endTime)) {
            throw new BadRequestException(
                    "Another show is already scheduled at this venue during the requested time");
        }

        // Create the show
        Show show = Show.builder()
                .showTime(request.getShowTime())
                .endTime(endTime)
                .status(ShowStatus.SCHEDULED)
                .basePrice(request.getBasePrice())
                .totalSeats(seats.size())
                .availableSeats(seats.size())
                .event(event)
                .venue(venue)
                .build();

        Show savedShow = showRepository.save(show);

        // Create ShowSeats for each seat
        List<ShowSeat> showSeats = createShowSeats(savedShow, seats, request);
        showSeatRepository.saveAll(showSeats);

        log.info("Show created with ID: {}, {} seats initialized",
                savedShow.getId(), showSeats.size());

        return entityMapper.toShowResponse(savedShow);
    }

    private List<ShowSeat> createShowSeats(Show show, List<Seat> seats, ShowRequest request) {
        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : seats) {
            Double price = calculateSeatPrice(request.getBasePrice(), seat.getCategory(), request);

            ShowSeat showSeat = ShowSeat.builder()
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .show(show)
                    .seat(seat)
                    .build();

            showSeats.add(showSeat);
        }

        return showSeats;
    }

    private Double calculateSeatPrice(Double basePrice,
                                      com.bookmyshow.eventservice.entity.enums.SeatCategory category,
                                      ShowRequest request) {
        return switch (category) {
            case SILVER -> basePrice * request.getSilverMultiplier();
            case GOLD -> basePrice * request.getGoldMultiplier();
            case PLATINUM -> basePrice * request.getPlatinumMultiplier();
            case VIP -> basePrice * request.getVipMultiplier();
            case RECLINER -> basePrice * request.getReclinerMultiplier();
        };
    }

    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", id));
        return entityMapper.toShowResponse(show);
    }

    public List<ShowResponse> getShowsByEvent(Long eventId) {
        // Verify event exists
        eventService.findEventById(eventId);
        List<Show> shows = showRepository.findByEventIdAndStatusNot(eventId, ShowStatus.CANCELLED);
        return entityMapper.toShowResponseList(shows);
    }

    public List<ShowSummaryResponse> getShowsForEventInCity(Long eventId, Long cityId) {
        List<Show> shows = showRepository.findShowsForEventInCity(
                eventId, cityId, LocalDateTime.now());
        return entityMapper.toShowSummaryResponseList(shows);
    }

    public List<ShowSummaryResponse> getShowsByVenue(Long venueId) {
        // Verify venue exists
        venueService.findVenueById(venueId);
        List<Show> shows = showRepository.findByVenueIdAndShowTimeAfter(
                venueId, LocalDateTime.now());
        return entityMapper.toShowSummaryResponseList(shows);
    }

    public List<ShowResponse> getUpcomingShows() {
        List<Show> shows = showRepository.findUpcomingShows(LocalDateTime.now());
        return entityMapper.toShowResponseList(shows);
    }

    @Transactional
    public ShowResponse updateShowStatus(Long id, ShowStatus status) {
        log.info("Updating show status: {} -> {}", id, status);

        Show show = findShowById(id);
        show.setStatus(status);

        Show updatedShow = showRepository.save(show);
        return entityMapper.toShowResponse(updatedShow);
    }

    @Transactional
    public ShowResponse openBooking(Long id) {
        log.info("Opening booking for show: {}", id);

        Show show = findShowById(id);

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new BadRequestException("Can only open booking for scheduled shows");
        }

        show.setStatus(ShowStatus.OPEN);
        Show updatedShow = showRepository.save(show);

        log.info("Booking opened for show: {}", id);
        return entityMapper.toShowResponse(updatedShow);
    }

    @Transactional
    public void cancelShow(Long id) {
        log.info("Cancelling show: {}", id);

        Show show = findShowById(id);

        if (show.getShowTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot cancel a show that has already started");
        }

        show.setStatus(ShowStatus.CANCELLED);
        showRepository.save(show);

        // Release all seats - set all to AVAILABLE and clear booking info
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(id);
        for (ShowSeat seat : showSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedByUserId(null);
            seat.setLockedAt(null);
            seat.setLockedUntil(null);
            seat.setBookedByUserId(null);
            seat.setBookingId(null);
        }
        showSeatRepository.saveAll(showSeats);

        log.info("Show cancelled: {}", id);
    }

    @Transactional
    public void updateAvailableSeats(Long showId) {
        Show show = findShowById(showId);
        int available = showSeatRepository.countAvailableSeats(showId, LocalDateTime.now());
        show.setAvailableSeats(available);

        // Update status based on availability
        if (available == 0) {
            show.setStatus(ShowStatus.SOLD_OUT);
        } else if (available <= show.getTotalSeats() * 0.2) {  // Less than 20%
            show.setStatus(ShowStatus.FAST_FILLING);
        } else if (show.getStatus() == ShowStatus.SOLD_OUT ||
                show.getStatus() == ShowStatus.FAST_FILLING) {
            show.setStatus(ShowStatus.OPEN);
        }

        showRepository.save(show);
    }

    // ============ Internal Methods ============

    public Show findShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", id));
    }
}