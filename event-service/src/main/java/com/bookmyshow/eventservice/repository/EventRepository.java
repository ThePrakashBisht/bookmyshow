package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.Event;
import com.bookmyshow.eventservice.entity.enums.EventStatus;
import com.bookmyshow.eventservice.entity.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find by type
    List<Event> findByEventTypeAndActiveTrue(EventType eventType);

    // Find by status
    List<Event> findByStatusAndActiveTrue(EventStatus status);

    // Search by title (case-insensitive, partial match)
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) AND e.active = true")
    List<Event> searchByTitle(@Param("title") String title);

    // Find by genre
    List<Event> findByGenreIgnoreCaseAndActiveTrue(String genre);

    // Find by language
    List<Event> findByLanguageIgnoreCaseAndActiveTrue(String language);

    // Find current events (released and not ended)
    @Query("SELECT e FROM Event e WHERE e.releaseDate <= :date AND (e.endDate IS NULL OR e.endDate >= :date) AND e.active = true ORDER BY e.rating DESC")
    List<Event> findCurrentEvents(@Param("date") LocalDate date);

    // Find upcoming events (not yet released)
    @Query("SELECT e FROM Event e WHERE e.releaseDate > :date AND e.active = true ORDER BY e.releaseDate ASC")
    List<Event> findUpcomingEvents(@Param("date") LocalDate date);

    // Complex search with pagination
    @Query("SELECT e FROM Event e WHERE " +
            "(:eventType IS NULL OR e.eventType = :eventType) AND " +
            "(:language IS NULL OR LOWER(e.language) = LOWER(:language)) AND " +
            "(:genre IS NULL OR LOWER(e.genre) = LOWER(:genre)) AND " +
            "e.active = true")
    Page<Event> searchEvents(@Param("eventType") EventType eventType,
                             @Param("language") String language,
                             @Param("genre") String genre,
                             Pageable pageable);

    // Find events with shows in a specific city
    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN e.shows s " +
            "JOIN s.venue v " +
            "WHERE v.city.id = :cityId AND e.active = true AND s.showTime > CURRENT_TIMESTAMP " +
            "ORDER BY e.rating DESC")
    List<Event> findEventsWithShowsInCity(@Param("cityId") Long cityId);

    // Count active events by type
    @Query("SELECT COUNT(e) FROM Event e WHERE e.eventType = :eventType AND e.active = true")
    long countByEventType(@Param("eventType") EventType eventType);
}