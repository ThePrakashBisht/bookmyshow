package com.bookmyshow.eventservice.repository;

import com.bookmyshow.eventservice.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCase(String name);

    List<City> findByActiveTrue();

    List<City> findByStateIgnoreCase(String state);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM City c WHERE c.active = true ORDER BY c.name")
    List<City> findAllActiveCitiesSorted();
}