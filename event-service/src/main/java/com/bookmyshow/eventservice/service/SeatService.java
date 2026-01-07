package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.BulkSeatRequest;
import com.bookmyshow.eventservice.dto.request.SeatRequest;
import com.bookmyshow.eventservice.dto.response.SeatLayoutResponse;
import com.bookmyshow.eventservice.dto.response.SeatResponse;
import com.bookmyshow.eventservice.entity.Seat;
import com.bookmyshow.eventservice.entity.Venue;
import com.bookmyshow.eventservice.exception.BadRequestException;
import com.bookmyshow.eventservice.exception.DuplicateResourceException;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.repository.SeatRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final VenueService venueService;
    private final EntityMapper entityMapper;

    @Transactional
    public SeatResponse createSeat(Long venueId, SeatRequest request) {
        log.info("Creating seat {}{} in venue: {}",
                request.getSeatRow(), request.getSeatNumber(), venueId);

        Venue venue = venueService.findVenueById(venueId);

        // Check if seat already exists
        if (seatRepository.existsByVenueIdAndSeatRowAndSeatNumber(
                venueId, request.getSeatRow().toUpperCase(), request.getSeatNumber())) {
            throw new DuplicateResourceException(
                    String.format("Seat %s%d already exists in venue",
                            request.getSeatRow(), request.getSeatNumber()));
        }

        Seat seat = Seat.builder()
                .seatRow(request.getSeatRow().toUpperCase())
                .seatNumber(request.getSeatNumber())
                .category(request.getCategory())
                .basePrice(request.getBasePrice())
                .venue(venue)
                .build();

        Seat savedSeat = seatRepository.save(seat);

        // Update venue seat count
        updateVenueSeatCount(venueId);

        log.info("Seat created with ID: {}", savedSeat.getId());
        return entityMapper.toSeatResponse(savedSeat);
    }

    @Transactional
    public List<SeatResponse> createBulkSeats(BulkSeatRequest request) {
        log.info("Creating bulk seats for venue: {}", request.getVenueId());

        Venue venue = venueService.findVenueById(request.getVenueId());
        List<Seat> createdSeats = new ArrayList<>();

        if (request.isRowConfigurationMode()) {
            // Mode 2: Multiple row configurations
            createdSeats = createSeatsFromRowConfigurations(venue, request.getRows());
        } else if (request.isSimpleRangeMode()) {
            // Mode 1: Simple range
            createdSeats = createSeatsFromSimpleRange(venue, request);
        } else {
            throw new BadRequestException(
                    "Either 'rows' configuration or 'startRow/endRow/seatsPerRow' must be provided");
        }

        List<Seat> savedSeats = seatRepository.saveAll(createdSeats);

        // Update venue seat count
        updateVenueSeatCount(request.getVenueId());

        log.info("Created {} seats for venue: {}", savedSeats.size(), request.getVenueId());
        return entityMapper.toSeatResponseList(savedSeats);
    }

    private List<Seat> createSeatsFromRowConfigurations(Venue venue,
                                                        List<BulkSeatRequest.RowConfiguration> rows) {
        List<Seat> seats = new ArrayList<>();

        for (BulkSeatRequest.RowConfiguration row : rows) {
            if (row.getStartSeat() > row.getEndSeat()) {
                throw new BadRequestException(
                        String.format("Invalid seat range for row %s: start %d > end %d",
                                row.getRowName(), row.getStartSeat(), row.getEndSeat()));
            }

            String rowName = row.getRowName().toUpperCase();

            for (int seatNum = row.getStartSeat(); seatNum <= row.getEndSeat(); seatNum++) {
                // Skip if seat already exists
                if (seatRepository.existsByVenueIdAndSeatRowAndSeatNumber(
                        venue.getId(), rowName, seatNum)) {
                    log.warn("Seat {}{} already exists, skipping", rowName, seatNum);
                    continue;
                }

                Seat seat = Seat.builder()
                        .seatRow(rowName)
                        .seatNumber(seatNum)
                        .category(row.getCategory())
                        .basePrice(row.getBasePrice())
                        .venue(venue)
                        .build();

                seats.add(seat);
            }
        }

        return seats;
    }

    private List<Seat> createSeatsFromSimpleRange(Venue venue, BulkSeatRequest request) {
        List<Seat> seats = new ArrayList<>();

        char startRow = request.getStartRow().toUpperCase().charAt(0);
        char endRow = request.getEndRow().toUpperCase().charAt(0);

        if (startRow > endRow) {
            throw new BadRequestException("Start row must be before or equal to end row");
        }

        for (char row = startRow; row <= endRow; row++) {
            String rowStr = String.valueOf(row);

            for (int seatNum = 1; seatNum <= request.getSeatsPerRow(); seatNum++) {
                // Skip if seat already exists
                if (seatRepository.existsByVenueIdAndSeatRowAndSeatNumber(
                        venue.getId(), rowStr, seatNum)) {
                    log.warn("Seat {}{} already exists, skipping", rowStr, seatNum);
                    continue;
                }

                Seat seat = Seat.builder()
                        .seatRow(rowStr)
                        .seatNumber(seatNum)
                        .category(request.getCategory())
                        .basePrice(request.getBasePrice())
                        .venue(venue)
                        .build();

                seats.add(seat);
            }
        }

        return seats;
    }

    public List<SeatResponse> getSeatsByVenue(Long venueId) {
        // Verify venue exists
        venueService.findVenueById(venueId);
        List<Seat> seats = seatRepository.findByVenueIdOrdered(venueId);
        return entityMapper.toSeatResponseList(seats);
    }

    public SeatLayoutResponse getSeatLayout(Long venueId) {
        Venue venue = venueService.findVenueById(venueId);
        List<Seat> seats = seatRepository.findByVenueIdOrdered(venueId);
        return entityMapper.toSeatLayoutResponse(venue, seats);
    }

    public SeatResponse getSeatById(Long id) {
        Seat seat = findSeatById(id);
        return entityMapper.toSeatResponse(seat);
    }

    @Transactional
    public void deleteSeat(Long venueId, Long seatId) {
        log.info("Deleting seat {} from venue {}", seatId, venueId);

        Seat seat = findSeatById(seatId);

        // Verify seat belongs to the venue
        if (!seat.getVenue().getId().equals(venueId)) {
            throw new BadRequestException("Seat does not belong to the specified venue");
        }

        seatRepository.delete(seat);

        // Update venue seat count
        updateVenueSeatCount(venueId);

        log.info("Seat deleted: {}", seat.getSeatLabel());
    }

    // ============ Internal Methods ============

    public Seat findSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", id));
    }

    public List<Seat> findSeatsByVenueId(Long venueId) {
        return seatRepository.findByVenueId(venueId);
    }

    private void updateVenueSeatCount(Long venueId) {
        int seatCount = seatRepository.countByVenueId(venueId);
        venueService.updateSeatCount(venueId, seatCount);
    }
}