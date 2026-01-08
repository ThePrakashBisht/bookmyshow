package com.bookmyshow.bookingservice.repository;

import com.bookmyshow.bookingservice.entity.Booking;
import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    List<Booking> findByShowId(Long showId);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.lockExpiresAt < :now")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED' WHERE b.status = 'PENDING' AND b.lockExpiresAt < :now")
    int expirePendingBookings(@Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.showId = :showId AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findActiveBookingsForShow(@Param("showId") Long showId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId AND b.status = 'CONFIRMED'")
    long countConfirmedBookingsByUser(@Param("userId") Long userId);

    boolean existsByBookingNumber(String bookingNumber);
}