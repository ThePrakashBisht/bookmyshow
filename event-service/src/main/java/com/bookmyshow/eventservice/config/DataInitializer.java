package com.bookmyshow.eventservice.config;

import com.bookmyshow.eventservice.entity.*;
import com.bookmyshow.eventservice.entity.enums.*;
import com.bookmyshow.eventservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (cityRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing sample data...");

        // Create Cities
        City mumbai = createCity("Mumbai", "Maharashtra");
        City delhi = createCity("Delhi", "Delhi");
        City bangalore = createCity("Bangalore", "Karnataka");
        City chennai = createCity("Chennai", "Tamil Nadu");

        // Create Venues
        Venue pvrPhoenix = createVenue("PVR Phoenix", "Lower Parel, Phoenix Mall", "400013", mumbai);
        Venue inoxNariman = createVenue("INOX Nariman Point", "Nariman Point", "400021", mumbai);
        Venue pvrSelect = createVenue("PVR Select Citywalk", "Saket", "110017", delhi);
        Venue inoxBangalore = createVenue("INOX Mantri Square", "Malleshwaram", "560003", bangalore);

        // Create Seats for venues
        createSeatsForVenue(pvrPhoenix);
        createSeatsForVenue(inoxNariman);
        createSeatsForVenue(pvrSelect);
        createSeatsForVenue(inoxBangalore);

        // Create Events
        Event avengers = createEvent(
                "Avengers: Endgame",
                "After the devastating events of Infinity War, the Avengers assemble once more to undo Thanos' actions and restore balance to the universe.",
                EventType.MOVIE, 181, "English", "Action", "UA",
                LocalDate.now().minusDays(5),
                List.of("Robert Downey Jr.", "Chris Evans", "Scarlett Johansson", "Chris Hemsworth"),
                List.of("Anthony Russo", "Joe Russo")
        );

        Event inception = createEvent(
                "Inception",
                "A thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea into the mind of a CEO.",
                EventType.MOVIE, 148, "English", "Sci-Fi", "UA",
                LocalDate.now().minusDays(3),
                List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Ellen Page", "Tom Hardy"),
                List.of("Christopher Nolan")
        );

        Event rrr = createEvent(
                "RRR",
                "A fictitious story about two legendary revolutionaries and their journey away from home before they started fighting for their country in the 1920s.",
                EventType.MOVIE, 187, "Telugu", "Action", "UA",
                LocalDate.now().minusDays(10),
                List.of("Jr NTR", "Ram Charan", "Alia Bhatt", "Ajay Devgn"),
                List.of("S. S. Rajamouli")
        );

        Event coldplay = createEvent(
                "Coldplay: Music of the Spheres Tour",
                "Experience Coldplay live in concert! The Music of the Spheres World Tour.",
                EventType.CONCERT, 180, "English", "Music", "U",
                LocalDate.now().plusDays(30),
                List.of("Chris Martin", "Jonny Buckland", "Guy Berryman", "Will Champion"),
                List.of()
        );

        // Create Shows
        createShow(avengers, pvrPhoenix, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), 250.0);
        createShow(avengers, pvrPhoenix, LocalDateTime.now().plusDays(1).withHour(14).withMinute(30), 300.0);
        createShow(avengers, pvrPhoenix, LocalDateTime.now().plusDays(1).withHour(18).withMinute(30), 350.0);
        createShow(avengers, inoxNariman, LocalDateTime.now().plusDays(1).withHour(15).withMinute(0), 280.0);
        createShow(avengers, pvrSelect, LocalDateTime.now().plusDays(1).withHour(19).withMinute(0), 320.0);

        createShow(inception, pvrPhoenix, LocalDateTime.now().plusDays(2).withHour(20).withMinute(0), 250.0);
        createShow(inception, pvrSelect, LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), 270.0);
        createShow(inception, inoxBangalore, LocalDateTime.now().plusDays(2).withHour(21).withMinute(0), 280.0);

        createShow(rrr, inoxNariman, LocalDateTime.now().plusDays(1).withHour(11).withMinute(0), 220.0);
        createShow(rrr, inoxBangalore, LocalDateTime.now().plusDays(1).withHour(16).withMinute(0), 240.0);

        log.info("Sample data initialization completed!");
        log.info("Created: {} cities, {} venues, {} events, {} shows",
                cityRepository.count(),
                venueRepository.count(),
                eventRepository.count(),
                showRepository.count());
    }

    private City createCity(String name, String state) {
        City city = City.builder()
                .name(name)
                .state(state)
                .country("India")
                .active(true)
                .build();
        return cityRepository.save(city);
    }

    private Venue createVenue(String name, String address, String pincode, City city) {
        Venue venue = Venue.builder()
                .name(name)
                .address(address)
                .pincode(pincode)
                .totalSeats(0)
                .active(true)
                .city(city)
                .build();
        return venueRepository.save(venue);
    }

    private void createSeatsForVenue(Venue venue) {
        int seatCount = 0;

        // Platinum rows (A, B) - 10 seats each @ ₹500
        for (String row : List.of("A", "B")) {
            for (int num = 1; num <= 10; num++) {
                createSeat(venue, row, num, SeatCategory.PLATINUM, 500.0);
                seatCount++;
            }
        }

        // Gold rows (C, D, E) - 12 seats each @ ₹350
        for (String row : List.of("C", "D", "E")) {
            for (int num = 1; num <= 12; num++) {
                createSeat(venue, row, num, SeatCategory.GOLD, 350.0);
                seatCount++;
            }
        }

        // Silver rows (F, G, H) - 15 seats each @ ₹200
        for (String row : List.of("F", "G", "H")) {
            for (int num = 1; num <= 15; num++) {
                createSeat(venue, row, num, SeatCategory.SILVER, 200.0);
                seatCount++;
            }
        }

        venue.setTotalSeats(seatCount);
        venueRepository.save(venue);

        log.info("Created {} seats for venue: {}", seatCount, venue.getName());
    }

    private void createSeat(Venue venue, String row, int number, SeatCategory category, double price) {
        Seat seat = Seat.builder()
                .seatRow(row)
                .seatNumber(number)
                .category(category)
                .basePrice(price)
                .venue(venue)
                .build();
        seatRepository.save(seat);
    }

    private Event createEvent(String title, String description, EventType type,
                              int duration, String language, String genre,
                              String certificate, LocalDate releaseDate,
                              List<String> cast, List<String> crew) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .eventType(type)
                .status(releaseDate.isAfter(LocalDate.now()) ? EventStatus.UPCOMING : EventStatus.ONGOING)
                .durationMinutes(duration)
                .language(language)
                .genre(genre)
                .certificate(certificate)
                .releaseDate(releaseDate)
                .cast(cast)
                .crew(crew)
                .rating(4.5)
                .totalVotes(1000L)
                .active(true)
                .build();
        return eventRepository.save(event);
    }

    private void createShow(Event event, Venue venue, LocalDateTime showTime, Double basePrice) {
        LocalDateTime endTime = showTime.plusMinutes(event.getDurationMinutes() + 15);

        Show show = Show.builder()
                .showTime(showTime)
                .endTime(endTime)
                .status(ShowStatus.OPEN)
                .basePrice(basePrice)
                .totalSeats(venue.getTotalSeats())
                .availableSeats(venue.getTotalSeats())
                .event(event)
                .venue(venue)
                .build();

        show = showRepository.save(show);

        // Create ShowSeats
        List<Seat> seats = seatRepository.findByVenueId(venue.getId());
        for (Seat seat : seats) {
            Double price = calculatePrice(basePrice, seat.getCategory());

            ShowSeat showSeat = ShowSeat.builder()
                    .status(SeatStatus.AVAILABLE)
                    .price(price)
                    .show(show)
                    .seat(seat)
                    .build();
            showSeatRepository.save(showSeat);
        }

        log.info("Created show: {} at {} - {}", event.getTitle(), venue.getName(), showTime);
    }

    private double calculatePrice(double basePrice, SeatCategory category) {
        return switch (category) {
            case SILVER -> basePrice * 1.0;
            case GOLD -> basePrice * 1.2;
            case PLATINUM -> basePrice * 1.5;
            case VIP -> basePrice * 2.0;
            case RECLINER -> basePrice * 1.8;
        };
    }
}