package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByCityIdAndActiveTrue(Long cityId);

    List<Venue> findByActiveTrue();

    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.id = :id")
    Optional<Venue> findByIdWithCity(@Param("id") Long id);

    @Query("SELECT v FROM Venue v WHERE v.city.name = :cityName AND v.active = true")
    List<Venue> findByCityName(@Param("cityName") String cityName);

    @Query("SELECT DISTINCT v FROM Venue v " +
            "JOIN v.shows s " +
            "WHERE s.event.id = :eventId AND v.city.id = :cityId AND v.active = true")
    List<Venue> findVenuesWithShowsForEvent(@Param("eventId") Long eventId,
                                            @Param("cityId") Long cityId);

    boolean existsByNameAndCityId(String name, Long cityId);
}