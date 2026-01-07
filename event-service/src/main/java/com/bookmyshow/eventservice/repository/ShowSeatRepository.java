package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.ShowSeat;
import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    // Find all seats for a show
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.show.id = :showId ORDER BY ss.seat.seatRow, ss.seat.seatNumber")
    List<ShowSeat> findByShowId(@Param("showId") Long showId);

    // Find seats by status for a show
    List<ShowSeat> findByShowIdAndStatus(Long showId, SeatStatus status);

    // Find available seats for a show (includes expired locks)
    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "WHERE ss.show.id = :showId " +
            "AND (ss.status = 'AVAILABLE' OR (ss.status = 'LOCKED' AND ss.lockedUntil < :now)) " +
            "ORDER BY s.seatRow, s.seatNumber")
    List<ShowSeat> findAvailableSeats(@Param("showId") Long showId, @Param("now") LocalDateTime now);

    // Find by show and seat
    Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

    // Find by ShowSeat IDs
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.id IN :ids")
    List<ShowSeat> findByIdIn(@Param("ids") List<Long> ids);

    // Find seats by category for a show
    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "WHERE ss.show.id = :showId AND s.category = :category " +
            "ORDER BY s.seatRow, s.seatNumber")
    List<ShowSeat> findByShowIdAndCategory(@Param("showId") Long showId,
                                           @Param("category") SeatCategory category);

    // Count available seats for a show
    @Query("SELECT COUNT(ss) FROM ShowSeat ss " +
            "WHERE ss.show.id = :showId " +
            "AND (ss.status = 'AVAILABLE' OR (ss.status = 'LOCKED' AND ss.lockedUntil < :now))")
    int countAvailableSeats(@Param("showId") Long showId, @Param("now") LocalDateTime now);

    // Count booked seats for a show
    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = 'BOOKED'")
    int countBookedSeats(@Param("showId") Long showId);

    // Count locked seats for a show (not expired)
    @Query("SELECT COUNT(ss) FROM ShowSeat ss " +
            "WHERE ss.show.id = :showId AND ss.status = 'LOCKED' AND ss.lockedUntil >= :now")
    int countLockedSeats(@Param("showId") Long showId, @Param("now") LocalDateTime now);

    // Find expired locked seats (for cleanup job)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.status = 'LOCKED' AND ss.lockedUntil < :now")
    List<ShowSeat> findExpiredLockedSeats(@Param("now") LocalDateTime now);

    // Release expired locks (bulk update)
    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', " +
            "ss.lockedByUserId = null, ss.lockedAt = null, ss.lockedUntil = null " +
            "WHERE ss.status = 'LOCKED' AND ss.lockedUntil < :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);

    // Find seats locked by a user
    List<ShowSeat> findByLockedByUserIdAndStatus(Long userId, SeatStatus status);

    // Find seats by booking ID
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.bookingId = :bookingId")
    List<ShowSeat> findByBookingId(@Param("bookingId") String bookingId);

    // Check if all seats in a list are available
    @Query("SELECT COUNT(ss) = :seatCount FROM ShowSeat ss " +
            "WHERE ss.id IN :seatIds " +
            "AND (ss.status = 'AVAILABLE' OR (ss.status = 'LOCKED' AND ss.lockedUntil < :now))")
    boolean areAllSeatsAvailable(@Param("seatIds") List<Long> seatIds,
                                 @Param("seatCount") long seatCount,
                                 @Param("now") LocalDateTime now);

    // Get total price for selected seats
    @Query("SELECT SUM(ss.price) FROM ShowSeat ss WHERE ss.id IN :seatIds")
    Double getTotalPriceForSeats(@Param("seatIds") List<Long> seatIds);
}