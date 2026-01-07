package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.SeatLockRequest;
import com.bookmyshow.eventservice.dto.response.SeatLockResponse;
import com.bookmyshow.eventservice.dto.response.ShowSeatLayoutResponse;
import com.bookmyshow.eventservice.dto.response.ShowSeatResponse;
import com.bookmyshow.eventservice.entity.Show;
import com.bookmyshow.eventservice.entity.ShowSeat;
import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import com.bookmyshow.eventservice.exception.BadRequestException;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.exception.SeatNotAvailableException;
import com.bookmyshow.eventservice.repository.ShowSeatRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowSeatService {

    private static final int LOCK_DURATION_MINUTES = 10;

    private final ShowSeatRepository showSeatRepository;
    private final ShowService showService;
    private final EntityMapper entityMapper;

    public ShowSeatLayoutResponse getSeatLayout(Long showId) {
        log.info("Getting seat layout for show: {}", showId);

        Show show = showService.findShowById(showId);
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);

        if (showSeats.isEmpty()) {
            throw new ResourceNotFoundException("No seats found for show: " + showId);
        }

        return entityMapper.toShowSeatLayoutResponse(show, showSeats);
    }

    public List<ShowSeatResponse> getAvailableSeats(Long showId) {
        log.info("Getting available seats for show: {}", showId);

        // Verify show exists
        showService.findShowById(showId);

        List<ShowSeat> availableSeats = showSeatRepository.findAvailableSeats(
                showId, LocalDateTime.now());

        return entityMapper.toShowSeatResponseList(availableSeats);
    }

    public List<ShowSeatResponse> getSeatsByCategory(Long showId, SeatCategory category) {
        log.info("Getting seats by category {} for show: {}", category, showId);

        // Verify show exists
        showService.findShowById(showId);

        List<ShowSeat> seats = showSeatRepository.findByShowIdAndCategory(showId, category);
        return entityMapper.toShowSeatResponseList(seats);
    }

    @Transactional
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        log.info("User {} attempting to lock {} seats for show {}",
                request.getUserId(), request.getShowSeatIds().size(), request.getShowId());

        // Verify show exists and is open for booking
        Show show = showService.findShowById(request.getShowId());
        validateShowForBooking(show);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockUntil = now.plusMinutes(LOCK_DURATION_MINUTES);

        List<ShowSeat> seatsToLock = new ArrayList<>();
        List<String> lockedSeatLabels = new ArrayList<>();
        double totalPrice = 0.0;

        // Fetch all requested seats
        List<ShowSeat> requestedSeats = showSeatRepository.findByIdIn(request.getShowSeatIds());

        if (requestedSeats.size() != request.getShowSeatIds().size()) {
            throw new ResourceNotFoundException("One or more seat IDs not found");
        }

        // Validate and prepare seats for locking
        for (ShowSeat showSeat : requestedSeats) {
            // Verify seat belongs to the correct show
            if (!showSeat.getShow().getId().equals(request.getShowId())) {
                throw new BadRequestException(
                        String.format("Seat %s does not belong to show %d",
                                showSeat.getSeat().getSeatLabel(), request.getShowId()));
            }

            // Check if seat is available
            if (!showSeat.isAvailable()) {
                throw new SeatNotAvailableException(
                        String.format("Seat %s is not available",
                                showSeat.getSeat().getSeatLabel()));
            }

            seatsToLock.add(showSeat);
            lockedSeatLabels.add(showSeat.getSeat().getSeatLabel());
            totalPrice += showSeat.getPrice();
        }

        // Lock all seats
        for (ShowSeat showSeat : seatsToLock) {
            showSeat.setStatus(SeatStatus.LOCKED);
            showSeat.setLockedByUserId(request.getUserId());
            showSeat.setLockedAt(now);
            showSeat.setLockedUntil(lockUntil);
        }

        List<ShowSeat> savedSeats = showSeatRepository.saveAll(seatsToLock);
        log.info("Successfully locked {} seats for user {}", savedSeats.size(), request.getUserId());

        // Update show available seats count
        showService.updateAvailableSeats(request.getShowId());

        // Build response
        List<Long> lockedSeatIds = savedSeats.stream()
                .map(ShowSeat::getId)
                .collect(Collectors.toList());

        return SeatLockResponse.builder()
                .success(true)
                .message("Seats locked successfully")
                .showId(request.getShowId())
                .userId(request.getUserId())
                .lockedSeatIds(lockedSeatIds)
                .lockedSeatLabels(lockedSeatLabels)
                .lockExpiresAt(lockUntil)
                .lockDurationMinutes(LOCK_DURATION_MINUTES)
                .totalPrice(totalPrice)
                .seatCount(lockedSeatIds.size())
                .build();
    }

    @Transactional
    public void releaseSeats(Long showId, List<Long> showSeatIds, Long userId) {
        log.info("Releasing seats for show: {}, user: {}", showId, userId);

        List<ShowSeat> seats = showSeatRepository.findByIdIn(showSeatIds);

        for (ShowSeat seat : seats) {
            // Verify seat belongs to the show
            if (!seat.getShow().getId().equals(showId)) {
                throw new BadRequestException("Seat does not belong to the specified show");
            }

            // Verify user owns the lock
            if (seat.getStatus() == SeatStatus.LOCKED &&
                    !userId.equals(seat.getLockedByUserId())) {
                throw new BadRequestException("You don't have permission to release this seat");
            }

            // Only release if locked (not booked)
            if (seat.getStatus() == SeatStatus.LOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedAt(null);
                seat.setLockedUntil(null);
                seat.setLockedByUserId(null);
            }
        }

        showSeatRepository.saveAll(seats);
        showService.updateAvailableSeats(showId);

        log.info("Released {} seats", seats.size());
    }

    @Transactional
    public List<ShowSeatResponse> confirmBooking(Long showId, List<Long> showSeatIds,
                                                 Long userId, String bookingId) {
        log.info("Confirming booking {} for user {} with {} seats",
                bookingId, userId, showSeatIds.size());

        List<ShowSeat> confirmedSeats = new ArrayList<>();
        List<ShowSeat> seats = showSeatRepository.findByIdIn(showSeatIds);

        for (ShowSeat showSeat : seats) {
            // Verify seat belongs to the show
            if (!showSeat.getShow().getId().equals(showId)) {
                throw new BadRequestException(
                        String.format("Seat %s does not belong to show %d",
                                showSeat.getSeat().getSeatLabel(), showId));
            }

            // Verify seat is locked by the same user
            if (showSeat.getStatus() != SeatStatus.LOCKED ||
                    !userId.equals(showSeat.getLockedByUserId())) {
                throw new SeatNotAvailableException(
                        String.format("Seat %s is not locked by you",
                                showSeat.getSeat().getSeatLabel()));
            }

            // Check if lock has expired
            if (showSeat.isLockExpired()) {
                throw new SeatNotAvailableException(
                        String.format("Lock expired for seat %s. Please select again.",
                                showSeat.getSeat().getSeatLabel()));
            }

            // Confirm the booking
            showSeat.setStatus(SeatStatus.BOOKED);
            showSeat.setBookedByUserId(userId);
            showSeat.setBookingId(bookingId);

            confirmedSeats.add(showSeat);
        }

        List<ShowSeat> savedSeats = showSeatRepository.saveAll(confirmedSeats);
        log.info("Confirmed {} seats for booking {}", savedSeats.size(), bookingId);

        // Update show available seats count
        showService.updateAvailableSeats(showId);

        return entityMapper.toShowSeatResponseList(savedSeats);
    }

    public List<ShowSeatResponse> getBookingDetails(String bookingId) {
        log.info("Getting booking details for: {}", bookingId);

        List<ShowSeat> seats = showSeatRepository.findByBookingId(bookingId);

        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }

        return entityMapper.toShowSeatResponseList(seats);
    }

    @Transactional
    public void cancelBooking(String bookingId, Long userId) {
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);

        List<ShowSeat> bookedSeats = showSeatRepository.findByBookingId(bookingId);

        if (bookedSeats.isEmpty()) {
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }

        // Verify user owns the booking
        ShowSeat firstSeat = bookedSeats.get(0);
        if (!userId.equals(firstSeat.getBookedByUserId())) {
            throw new BadRequestException("You don't have permission to cancel this booking");
        }

        Long showId = firstSeat.getShow().getId();

        // Release all seats
        for (ShowSeat showSeat : bookedSeats) {
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeat.setBookedByUserId(null);
            showSeat.setBookingId(null);
            showSeat.setLockedByUserId(null);
            showSeat.setLockedAt(null);
            showSeat.setLockedUntil(null);
        }

        showSeatRepository.saveAll(bookedSeats);

        // Update show available seats count
        showService.updateAvailableSeats(showId);

        log.info("Cancelled booking {} - released {} seats", bookingId, bookedSeats.size());
    }

    // Scheduled task to release expired locks (runs every minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        int released = showSeatRepository.releaseExpiredLocks(LocalDateTime.now());
        if (released > 0) {
            log.info("Released {} expired seat locks", released);
        }
    }

    // ============ Helper Methods ============

    private void validateShowForBooking(Show show) {
        switch (show.getStatus()) {
            case CANCELLED:
                throw new BadRequestException("This show has been cancelled");
            case COMPLETED:
                throw new BadRequestException("This show has already ended");
            case SOLD_OUT:
                throw new BadRequestException("This show is sold out");
            case SCHEDULED:
                throw new BadRequestException("Booking is not yet open for this show");
            default:
                // OPEN, FAST_FILLING - proceed
                break;
        }
    }

    public ShowSeat findShowSeatById(Long id) {
        return showSeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShowSeat", "id", id));
    }
}