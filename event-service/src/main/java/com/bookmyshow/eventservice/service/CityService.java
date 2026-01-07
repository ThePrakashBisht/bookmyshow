package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.CityRequest;
import com.bookmyshow.eventservice.dto.response.CityResponse;
import com.bookmyshow.eventservice.entity.City;
import com.bookmyshow.eventservice.exception.DuplicateResourceException;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.repository.CityRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityService {

    private final CityRepository cityRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public CityResponse createCity(CityRequest request) {
        log.info("Creating city: {}", request.getName());

        // Check if city already exists
        if (cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("City", "name", request.getName());
        }

        City city = City.builder()
                .name(request.getName())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .active(true)
                .build();

        City savedCity = cityRepository.save(city);
        log.info("City created with ID: {}", savedCity.getId());

        return entityMapper.toCityResponse(savedCity);
    }

    public CityResponse getCityById(Long id) {
        City city = findCityById(id);
        return entityMapper.toCityResponse(city);
    }

    public List<CityResponse> getAllCities() {
        List<City> cities = cityRepository.findAllActiveCitiesSorted();
        return entityMapper.toCityResponseList(cities);
    }

    public List<CityResponse> getCitiesByState(String state) {
        List<City> cities = cityRepository.findByStateIgnoreCase(state);
        return entityMapper.toCityResponseList(cities);
    }

    @Transactional
    public CityResponse updateCity(Long id, CityRequest request) {
        log.info("Updating city with ID: {}", id);

        City city = findCityById(id);

        // Check if new name conflicts with another city
        cityRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(existingCity -> {
                    if (!existingCity.getId().equals(id)) {
                        throw new DuplicateResourceException("City", "name", request.getName());
                    }
                });

        city.setName(request.getName());
        city.setState(request.getState());
        if (request.getCountry() != null) {
            city.setCountry(request.getCountry());
        }

        City updatedCity = cityRepository.save(city);
        log.info("City updated: {}", updatedCity.getName());

        return entityMapper.toCityResponse(updatedCity);
    }

    @Transactional
    public void deleteCity(Long id) {
        log.info("Deleting city with ID: {}", id);

        City city = findCityById(id);
        city.setActive(false);  // Soft delete
        cityRepository.save(city);

        log.info("City soft deleted: {}", city.getName());
    }

    @Transactional
    public CityResponse toggleStatus(Long id) {
        log.info("Toggling status for city: {}", id);

        City city = findCityById(id);
        city.setActive(!city.isActive());
        City updatedCity = cityRepository.save(city);

        log.info("City {} status changed to: {}", id, updatedCity.isActive());
        return entityMapper.toCityResponse(updatedCity);
    }

    // ============ Internal Methods ============

    public City findCityById(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));
    }
}