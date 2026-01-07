package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.Show;
import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // Find shows for an event (excluding cancelled)
    List<Show> findByEventIdAndStatusNot(Long eventId, ShowStatus status);

    // Find shows at a venue after a certain time
    List<Show> findByVenueIdAndShowTimeAfter(Long venueId, LocalDateTime after);

    // Find shows for an event at a venue after a certain time
    List<Show> findByEventIdAndVenueIdAndShowTimeAfter(Long eventId, Long venueId, LocalDateTime after);

    // Find upcoming shows (not cancelled)
    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.event " +
            "JOIN FETCH s.venue " +
            "WHERE s.showTime > :now AND s.status != 'CANCELLED' " +
            "ORDER BY s.showTime")
    List<Show> findUpcomingShows(@Param("now") LocalDateTime now);

    // Find shows for event in a city
    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.event " +
            "JOIN FETCH s.venue v " +
            "JOIN FETCH v.city " +
            "WHERE s.event.id = :eventId " +
            "AND v.city.id = :cityId " +
            "AND s.showTime > :now " +
            "AND s.status != 'CANCELLED' " +
            "ORDER BY s.showTime")
    List<Show> findShowsForEventInCity(@Param("eventId") Long eventId,
                                       @Param("cityId") Long cityId,
                                       @Param("now") LocalDateTime now);

    // Find show with venue and event loaded (for detailed view)
    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.event " +
            "JOIN FETCH s.venue v " +
            "JOIN FETCH v.city " +
            "WHERE s.id = :showId")
    Optional<Show> findByIdWithDetails(@Param("showId") Long showId);

    // Check for conflicting shows (same venue, overlapping time)
    @Query("SELECT COUNT(s) > 0 FROM Show s " +
            "WHERE s.venue.id = :venueId " +
            "AND s.status != 'CANCELLED' " +
            "AND s.id != :excludeShowId " +
            "AND ((s.showTime BETWEEN :startTime AND :endTime) " +
            "OR (s.endTime BETWEEN :startTime AND :endTime) " +
            "OR (s.showTime <= :startTime AND s.endTime >= :endTime))")
    boolean existsConflictingShow(@Param("venueId") Long venueId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("excludeShowId") Long excludeShowId);

    // Overloaded method for new shows (no ID to exclude)
    default boolean existsConflictingShow(Long venueId, LocalDateTime startTime, LocalDateTime endTime) {
        return existsConflictingShow(venueId, startTime, endTime, -1L);
    }

    // Find shows on a specific date for an event
    @Query("SELECT s FROM Show s " +
            "WHERE s.event.id = :eventId " +
            "AND CAST(s.showTime AS date) = CAST(:date AS date) " +
            "AND s.status != 'CANCELLED' " +
            "ORDER BY s.showTime")
    List<Show> findShowsForEventOnDate(@Param("eventId") Long eventId,
                                       @Param("date") LocalDateTime date);

    // Update show status
    @Modifying
    @Query("UPDATE Show s SET s.status = :status WHERE s.id = :showId")
    int updateShowStatus(@Param("showId") Long showId, @Param("status") ShowStatus status);

    // Find shows that have ended but not marked as completed
    @Query("SELECT s FROM Show s WHERE s.endTime < :now AND s.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Show> findEndedShowsNotCompleted(@Param("now") LocalDateTime now);
}