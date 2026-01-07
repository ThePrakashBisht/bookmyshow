package com.bookmyshow.eventservice.util;

import com.bookmyshow.eventservice.dto.response.*;
import com.bookmyshow.eventservice.entity.*;
import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // ============ CITY MAPPERS ============

    public CityResponse toCityResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .state(city.getState())
                .country(city.getCountry())
                .active(city.isActive())
                .venueCount(city.getVenues() != null ? city.getVenues().size() : 0)
                .build();
    }

    public List<CityResponse> toCityResponseList(List<City> cities) {
        return cities.stream()
                .map(this::toCityResponse)
                .collect(Collectors.toList());
    }

    // ============ VENUE MAPPERS ============

    public VenueResponse toVenueResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .pincode(venue.getPincode())
                .totalSeats(venue.getTotalSeats())
                .contactNumber(venue.getContactNumber())
                .email(venue.getEmail())
                .active(venue.isActive())
                .city(venue.getCity() != null ? toCityResponse(venue.getCity()) : null)
                .build();
    }

    public VenueSummaryResponse toVenueSummaryResponse(Venue venue) {
        return VenueSummaryResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .cityName(venue.getCity() != null ? venue.getCity().getName() : null)
                .totalSeats(venue.getTotalSeats())
                .build();
    }

    public List<VenueResponse> toVenueResponseList(List<Venue> venues) {
        return venues.stream()
                .map(this::toVenueResponse)
                .collect(Collectors.toList());
    }

    // ============ SEAT MAPPERS ============

    public SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatLabel(seat.getSeatLabel())
                .category(seat.getCategory())
                .basePrice(seat.getBasePrice())
                .venueId(seat.getVenue().getId())
                .build();
    }

    public List<SeatResponse> toSeatResponseList(List<Seat> seats) {
        return seats.stream()
                .map(this::toSeatResponse)
                .collect(Collectors.toList());
    }

    public SeatLayoutResponse toSeatLayoutResponse(Venue venue, List<Seat> seats) {
        // Group seats by row
        Map<String, List<SeatResponse>> seatsByRow = seats.stream()
                .map(this::toSeatResponse)
                .collect(Collectors.groupingBy(
                        SeatResponse::getSeatRow,
                        TreeMap::new,
                        Collectors.toList()
                ));

        // Sort seats within each row
        seatsByRow.values().forEach(rowSeats ->
                rowSeats.sort(Comparator.comparing(SeatResponse::getSeatNumber)));

        // Count by category
        Map<SeatCategory, Integer> seatCountByCategory = seats.stream()
                .collect(Collectors.groupingBy(
                        Seat::getCategory,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Price by category
        Map<SeatCategory, Double> priceByCategory = seats.stream()
                .collect(Collectors.toMap(
                        Seat::getCategory,
                        Seat::getBasePrice,
                        (p1, p2) -> p1
                ));

        return SeatLayoutResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalSeats(seats.size())
                .seatsByRow(seatsByRow)
                .seatCountByCategory(seatCountByCategory)
                .priceByCategory(priceByCategory)
                .build();
    }

    // ============ EVENT MAPPERS ============

    public EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .status(event.getStatus())
                .durationMinutes(event.getDurationMinutes())
                .language(event.getLanguage())
                .genre(event.getGenre())
                .certificate(event.getCertificate())
                .releaseDate(event.getReleaseDate())
                .endDate(event.getEndDate())
                .posterUrl(event.getPosterUrl())
                .bannerUrl(event.getBannerUrl())
                .trailerUrl(event.getTrailerUrl())
                .rating(event.getRating())
                .totalVotes(event.getTotalVotes())
                .cast(event.getCast())
                .crew(event.getCrew())
                .active(event.isActive())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public EventSummaryResponse toEventSummaryResponse(Event event) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventType(event.getEventType())
                .language(event.getLanguage())
                .genre(event.getGenre())
                .certificate(event.getCertificate())
                .durationMinutes(event.getDurationMinutes())
                .releaseDate(event.getReleaseDate())
                .posterUrl(event.getPosterUrl())
                .rating(event.getRating())
                .totalVotes(event.getTotalVotes())
                .build();
    }

    public List<EventResponse> toEventResponseList(List<Event> events) {
        return events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    public List<EventSummaryResponse> toEventSummaryResponseList(List<Event> events) {
        return events.stream()
                .map(this::toEventSummaryResponse)
                .collect(Collectors.toList());
    }

    // ============ SHOW MAPPERS ============

    public ShowResponse toShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .showTime(show.getShowTime())
                .endTime(show.getEndTime())
                .status(show.getStatus())
                .basePrice(show.getBasePrice())
                .availableSeats(show.getAvailableSeats())
                .totalSeats(show.getTotalSeats())
                .event(show.getEvent() != null ? toEventSummaryResponse(show.getEvent()) : null)
                .venue(show.getVenue() != null ? toVenueSummaryResponse(show.getVenue()) : null)
                .build();
    }

    public ShowSummaryResponse toShowSummaryResponse(Show show) {
        return ShowSummaryResponse.builder()
                .id(show.getId())
                .showTime(show.getShowTime())
                .time(LocalTime.from(show.getShowTime()))
                .status(show.getStatus())
                .basePrice(show.getBasePrice())
                .availableSeats(show.getAvailableSeats())
                .totalSeats(show.getTotalSeats())
                .venueId(show.getVenue() != null ? show.getVenue().getId() : null)
                .venueName(show.getVenue() != null ? show.getVenue().getName() : null)
                .build();
    }

    public List<ShowResponse> toShowResponseList(List<Show> shows) {
        return shows.stream()
                .map(this::toShowResponse)
                .collect(Collectors.toList());
    }

    public List<ShowSummaryResponse> toShowSummaryResponseList(List<Show> shows) {
        return shows.stream()
                .map(this::toShowSummaryResponse)
                .collect(Collectors.toList());
    }

    // ============ SHOW SEAT MAPPERS ============

    public ShowSeatResponse toShowSeatResponse(ShowSeat showSeat) {
        Seat seat = showSeat.getSeat();
        boolean isAvailable = showSeat.getStatus() == SeatStatus.AVAILABLE ||
                (showSeat.getStatus() == SeatStatus.LOCKED && showSeat.isLockExpired());

        return ShowSeatResponse.builder()
                .id(showSeat.getId())
                .seatId(seat.getId())
                .showId(showSeat.getShow().getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatLabel(seat.getSeatLabel())
                .category(seat.getCategory())
                .status(isAvailable ? SeatStatus.AVAILABLE : showSeat.getStatus())
                .price(showSeat.getPrice())
                .available(isAvailable)
                .build();
    }

    public List<ShowSeatResponse> toShowSeatResponseList(List<ShowSeat> showSeats) {
        return showSeats.stream()
                .map(this::toShowSeatResponse)
                .collect(Collectors.toList());
    }

    public ShowSeatLayoutResponse toShowSeatLayoutResponse(Show show, List<ShowSeat> showSeats) {
        LocalDateTime now = LocalDateTime.now();

        // Map to responses first
        List<ShowSeatResponse> seatResponses = showSeats.stream()
                .map(this::toShowSeatResponse)
                .collect(Collectors.toList());

        // Group seats by row
        Map<String, List<ShowSeatResponse>> seatsByRow = seatResponses.stream()
                .collect(Collectors.groupingBy(
                        ShowSeatResponse::getSeatRow,
                        TreeMap::new,
                        Collectors.toList()
                ));

        // Sort seats within each row
        seatsByRow.values().forEach(rowSeats ->
                rowSeats.sort(Comparator.comparing(ShowSeatResponse::getSeatNumber)));

        // Count available seats
        int availableCount = (int) seatResponses.stream()
                .filter(ShowSeatResponse::isAvailable)
                .count();

        int bookedCount = (int) showSeats.stream()
                .filter(ss -> ss.getStatus() == SeatStatus.BOOKED)
                .count();

        int lockedCount = (int) showSeats.stream()
                .filter(ss -> ss.getStatus() == SeatStatus.LOCKED && !ss.isLockExpired())
                .count();

        // Price by category
        Map<SeatCategory, Double> priceByCategory = showSeats.stream()
                .collect(Collectors.toMap(
                        ss -> ss.getSeat().getCategory(),
                        ShowSeat::getPrice,
                        (p1, p2) -> p1
                ));

        // Available count by category
        Map<SeatCategory, Integer> availableByCategory = seatResponses.stream()
                .filter(ShowSeatResponse::isAvailable)
                .collect(Collectors.groupingBy(
                        ShowSeatResponse::getCategory,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return ShowSeatLayoutResponse.builder()
                .showId(show.getId())
                .eventTitle(show.getEvent().getTitle())
                .venueId(show.getVenue().getId())
                .venueName(show.getVenue().getName())
                .totalSeats(showSeats.size())
                .availableSeats(availableCount)
                .bookedSeats(bookedCount)
                .lockedSeats(lockedCount)
                .seatsByRow(seatsByRow)
                .priceByCategory(priceByCategory)
                .availableByCategory(availableByCategory)
                .build();
    }
}