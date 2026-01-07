package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.Seat;
import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueId(Long venueId);

    List<Seat> findByVenueIdAndCategory(Long venueId, SeatCategory category);

    Optional<Seat> findByVenueIdAndSeatRowAndSeatNumber(Long venueId, String seatRow, Integer seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.venue.id = :venueId ORDER BY s.seatRow, s.seatNumber")
    List<Seat> findByVenueIdOrdered(@Param("venueId") Long venueId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.venue.id = :venueId")
    int countByVenueId(@Param("venueId") Long venueId);

    boolean existsByVenueIdAndSeatRowAndSeatNumber(Long venueId, String seatRow, Integer seatNumber);

    @Query("SELECT s FROM Seat s WHERE s.venue.id = :venueId AND s.category = :category ORDER BY s.seatRow, s.seatNumber")
    List<Seat> findByVenueIdAndCategoryOrdered(@Param("venueId") Long venueId,
                                               @Param("category") SeatCategory category);
}