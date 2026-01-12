package com.bookmyshow.bookingservice.service;

import com.bookmyshow.bookingservice.dto.external.ShowInfo;
import com.bookmyshow.bookingservice.dto.external.ShowSeatInfo;
import com.bookmyshow.bookingservice.dto.request.CancelBookingRequest;
import com.bookmyshow.bookingservice.dto.request.ConfirmPaymentRequest;
import com.bookmyshow.bookingservice.dto.request.InitiateBookingRequest;
import com.bookmyshow.bookingservice.dto.response.BookingResponse;
import com.bookmyshow.bookingservice.dto.response.BookingSummaryResponse;
import com.bookmyshow.bookingservice.dto.response.PaymentResponse;
import com.bookmyshow.bookingservice.entity.Booking;
import com.bookmyshow.bookingservice.entity.BookingItem;
import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import com.bookmyshow.bookingservice.entity.enums.PaymentStatus;
import com.bookmyshow.bookingservice.exception.*;
import com.bookmyshow.bookingservice.repository.BookingRepository;
import com.bookmyshow.bookingservice.service.external.EventServiceClient;
import com.bookmyshow.bookingservice.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisLockService redisLockService;
    private final EventServiceClient eventServiceClient;
    private final PaymentService paymentService;
    private final BookingMapper bookingMapper;

    @Value("${booking.lock.duration-seconds:300}")
    private int lockDurationSeconds;

    private static final double CONVENIENCE_FEE_PERCENT = 2.0;  // 2% convenience fee
    private static final double TAX_PERCENT = 18.0;             // 18% GST

    // ============ INITIATE BOOKING (Step 1) ============

    @Transactional
    public BookingResponse initiateBooking(InitiateBookingRequest request) {
        log.info("Initiating booking for user {} with {} seats for show {}",
                request.getUserId(), request.getShowSeatIds().size(), request.getShowId());

        // 1. Fetch show details from Event Service
        ShowInfo showInfo = eventServiceClient.getShowDetails(request.getShowId());
        validateShowForBooking(showInfo);

        // 2. Fetch seat details from Event Service
        List<ShowSeatInfo> seats = eventServiceClient.getShowSeats(
                request.getShowId(), request.getShowSeatIds());

        if (seats.size() != request.getShowSeatIds().size()) {
            throw new BadRequestException("Some seats were not found");
        }

        // 3. Validate all seats are available
        validateSeatsAvailable(seats);

        // 4. Generate lock ID and try to acquire Redis locks
        String lockId = redisLockService.generateLockId();
        boolean lockAcquired = redisLockService.lockSeats(
                request.getShowId(),
                request.getShowSeatIds(),
                request.getUserId(),
                lockId
        );

        if (!lockAcquired) {
            throw new SeatLockException("Unable to lock seats. Some seats may already be selected by others.");
        }

        try {
            // 5. Calculate pricing
            double subtotal = seats.stream()
                    .mapToDouble(ShowSeatInfo::getPrice)
                    .sum();
            double convenienceFee = (subtotal * CONVENIENCE_FEE_PERCENT) / 100;
            double taxAmount = ((subtotal + convenienceFee) * TAX_PERCENT) / 100;
            double totalAmount = subtotal + convenienceFee + taxAmount;

            // 6. Create booking entity
            LocalDateTime lockExpiresAt = LocalDateTime.now().plusSeconds(lockDurationSeconds);

            Booking booking = Booking.builder()
                    .userId(request.getUserId())
                    .showId(request.getShowId())
                    .eventId(showInfo.getEventId())
                    .eventTitle(showInfo.getEventTitle())
                    .venueId(showInfo.getVenueId())
                    .venueName(showInfo.getVenueName())
                    .showTime(showInfo.getShowTime())
                    .status(BookingStatus.PENDING)
                    .totalSeats(seats.size())
                    .subtotal(subtotal)
                    .convenienceFee(convenienceFee)
                    .taxAmount(taxAmount)
                    .totalAmount(totalAmount)
                    .paymentStatus(PaymentStatus.PENDING)
                    .lockExpiresAt(lockExpiresAt)
                    .userEmail(request.getUserEmail())
                    .userPhone(request.getUserPhone())
                    .build();

            // 7. Create booking items
            for (ShowSeatInfo seat : seats) {
                BookingItem item = BookingItem.builder()
                        .showSeatId(seat.getId())
                        .seatId(seat.getSeatId())
                        .seatLabel(seat.getSeatLabel())
                        .seatRow(seat.getSeatRow())
                        .seatNumber(seat.getSeatNumber())
                        .seatCategory(seat.getCategory())
                        .price(seat.getPrice())
                        .build();
                booking.addBookingItem(item);
            }

            // 8. Save booking
            Booking savedBooking = bookingRepository.save(booking);

            log.info("Booking initiated successfully. Booking number: {}, Lock expires at: {}",
                    savedBooking.getBookingNumber(), lockExpiresAt);

            return bookingMapper.toBookingResponse(savedBooking);

        } catch (Exception e) {
            // Release locks if booking creation fails
            log.error("Booking creation failed, releasing locks: {}", e.getMessage());
            redisLockService.releaseSeats(request.getShowId(), request.getShowSeatIds(), lockId);
            throw e;
        }
    }

    // ============ CONFIRM PAYMENT (Step 2) ============

    @Transactional
    public BookingResponse confirmPayment(ConfirmPaymentRequest request) {
        log.info("Confirming payment for booking: {}", request.getBookingNumber());

        // 1. Find booking
        Booking booking = findBookingByNumber(request.getBookingNumber());

        // 2. Validate booking ownership
        if (!booking.getUserId().equals(request.getUserId())) {
            throw new BadRequestException("You don't have permission to pay for this booking");
        }

        // 3. Check booking status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in pending state. Current status: " + booking.getStatus());
        }

        // 4. Check if lock expired
        if (booking.isLockExpired()) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new BookingExpiredException(booking.getBookingNumber());
        }

        // 5. Process payment
        paymentService.validatePaymentMethod(request.getPaymentMethod());

        PaymentResponse paymentResponse = paymentService.processPayment(
                booking.getBookingNumber(),
                booking.getTotalAmount(),
                request.getPaymentMethod(),
                request.getPaymentToken(),
                request.isSimulateSuccess()
        );

        if (!paymentResponse.isSuccess()) {
            // Payment failed
            booking.setPaymentStatus(PaymentStatus.FAILED);
            bookingRepository.save(booking);
            throw new PaymentException("Payment failed: " + paymentResponse.getMessage());
        }

        // 6. Payment successful - update booking
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setPaymentTransactionId(paymentResponse.getTransactionId());
        booking.setPaymentTime(LocalDateTime.now());

        // 7. Confirm seats with Event Service
        List<Long> showSeatIds = booking.getBookingItems().stream()
                .map(BookingItem::getShowSeatId)
                .collect(Collectors.toList());

        boolean seatsConfirmed = eventServiceClient.confirmSeatsBooked(
                booking.getShowId(),
                showSeatIds,
                booking.getUserId(),
                booking.getBookingNumber()
        );

        if (!seatsConfirmed) {
            log.warn("Failed to confirm seats with Event Service for booking: {}. " +
                    "This should be retried.", booking.getBookingNumber());
            // In production, you would add this to a retry queue
        }

        // 8. Release Redis locks (seats are now permanently booked)
        String lockId = redisLockService.generateLockId(); // Note: In production, store lockId in booking
        redisLockService.releaseSeats(booking.getShowId(), showSeatIds, lockId);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking confirmed successfully. Booking number: {}, Transaction: {}",
                savedBooking.getBookingNumber(), paymentResponse.getTransactionId());

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // ============ CANCEL BOOKING ============

    @Transactional
    public BookingResponse cancelBooking(CancelBookingRequest request) {
        log.info("Cancelling booking: {} by user: {}", request.getBookingNumber(), request.getUserId());

        // 1. Find booking
        Booking booking = findBookingByNumber(request.getBookingNumber());

        // 2. Validate ownership
        if (!booking.getUserId().equals(request.getUserId())) {
            throw new BadRequestException("You don't have permission to cancel this booking");
        }

        // 3. Check if cancellable
        if (!booking.isCancellable()) {
            throw new BadRequestException("This booking cannot be cancelled. Status: " + booking.getStatus());
        }

        // 4. Get seat IDs for releasing
        List<Long> showSeatIds = booking.getBookingItems().stream()
                .map(BookingItem::getShowSeatId)
                .collect(Collectors.toList());

        // 5. Release seats in Event Service
        eventServiceClient.releaseSeats(booking.getShowId(), showSeatIds, booking.getUserId());

        // 6. Process refund if payment was made
        Double refundAmount = 0.0;
        if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
            PaymentResponse refundResponse = paymentService.processRefund(
                    booking.getBookingNumber(),
                    booking.getPaymentTransactionId(),
                    booking.getTotalAmount()
            );
            if (refundResponse.isSuccess()) {
                refundAmount = booking.getTotalAmount();
            }
        }

        // 7. Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(request.getReason());
        booking.setRefundAmount(refundAmount);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking cancelled successfully. Booking number: {}, Refund: {}",
                savedBooking.getBookingNumber(), refundAmount);

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // ============ QUERY METHODS ============

    public BookingResponse getBookingByNumber(String bookingNumber) {
        Booking booking = findBookingByNumber(bookingNumber);
        return bookingMapper.toBookingResponse(booking);
    }

//    @Transactional(readOnly = true)
//    public BookingResponse getBookingById(Long bookingId) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
//
//        return bookingMapper.toBookingResponse(booking);
//    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return bookingMapper.toBookingResponse(booking);
    }

    public List<BookingSummaryResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookingMapper.toBookingSummaryResponseList(bookings);
    }

    public List<BookingSummaryResponse> getUserBookingsByStatus(Long userId, BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByUserIdAndStatus(userId, status);
        return bookingMapper.toBookingSummaryResponseList(bookings);
    }

    // ============ SCHEDULED JOBS ============

    /**
     * Expire pending bookings whose lock has expired.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePendingBookings() {
        log.debug("Running scheduled job: expire pending bookings");

        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(LocalDateTime.now());

        for (Booking booking : expiredBookings) {
            try {
                log.info("Expiring booking: {}", booking.getBookingNumber());

                // Release seats in Event Service
                List<Long> showSeatIds = booking.getBookingItems().stream()
                        .map(BookingItem::getShowSeatId)
                        .collect(Collectors.toList());

                eventServiceClient.releaseSeats(booking.getShowId(), showSeatIds, booking.getUserId());

                // Update status
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);

                log.info("Booking expired: {}", booking.getBookingNumber());

            } catch (Exception e) {
                log.error("Error expiring booking {}: {}", booking.getBookingNumber(), e.getMessage());
            }
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Expired {} pending bookings", expiredBookings.size());
        }
    }

    // ============ HELPER METHODS ============

    private Booking findBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingNumber", bookingNumber));
    }

    private void validateShowForBooking(ShowInfo showInfo) {
        if (showInfo == null) {
            throw new ResourceNotFoundException("Show not found");
        }

        String status = showInfo.getStatus();
        if ("CANCELLED".equals(status)) {
            throw new BadRequestException("This show has been cancelled");
        }
        if ("COMPLETED".equals(status)) {
            throw new BadRequestException("This show has already ended");
        }
        if ("SOLD_OUT".equals(status)) {
            throw new BadRequestException("This show is sold out");
        }
        if (showInfo.getShowTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book for a show that has already started");
        }
    }

    private void validateSeatsAvailable(List<ShowSeatInfo> seats) {
        for (ShowSeatInfo seat : seats) {
            if (!seat.isAvailable()) {
                throw new SeatLockException(seat.getSeatLabel(), "Seat is not available");
            }
        }
    }
}