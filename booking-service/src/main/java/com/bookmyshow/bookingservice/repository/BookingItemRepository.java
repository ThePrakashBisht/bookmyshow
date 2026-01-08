package com.bookmyshow.bookingservice.repository;

import com.bookmyshow.bookingservice.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    List<BookingItem> findByBookingId(Long bookingId);

    @Query("SELECT bi FROM BookingItem bi WHERE bi.booking.showId = :showId AND bi.booking.status IN ('PENDING', 'CONFIRMED')")
    List<BookingItem> findActiveItemsForShow(@Param("showId") Long showId);

    @Query("SELECT bi.showSeatId FROM BookingItem bi WHERE bi.booking.showId = :showId AND bi.booking.status IN ('PENDING', 'CONFIRMED')")
    List<Long> findLockedSeatIdsForShow(@Param("showId") Long showId);
}