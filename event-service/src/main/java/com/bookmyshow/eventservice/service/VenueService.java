package com.bookmyshow.eventservice.service;

import com.bookmyshow.eventservice.dto.request.VenueRequest;
import com.bookmyshow.eventservice.dto.response.VenueResponse;
import com.bookmyshow.eventservice.entity.City;
import com.bookmyshow.eventservice.entity.Venue;
import com.bookmyshow.eventservice.exception.DuplicateResourceException;
import com.bookmyshow.eventservice.exception.ResourceNotFoundException;
import com.bookmyshow.eventservice.repository.VenueRepository;
import com.bookmyshow.eventservice.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueService {

    private final VenueRepository venueRepository;
    private final CityService cityService;
    private final EntityMapper entityMapper;

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        log.info("Creating venue: {} in city: {}", request.getName(), request.getCityId());

        City city = cityService.findCityById(request.getCityId());

        // Check for duplicate venue in same city
        if (venueRepository.existsByNameAndCityId(request.getName(), request.getCityId())) {
            throw new DuplicateResourceException(
                    String.format("Venue '%s' already exists in city '%s'",
                            request.getName(), city.getName()));
        }

        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .pincode(request.getPincode())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .totalSeats(0)  // Will be updated when seats are added
                .active(true)
                .city(city)
                .build();

        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue created with ID: {}", savedVenue.getId());

        return entityMapper.toVenueResponse(savedVenue);
    }

    public VenueResponse getVenueById(Long id) {
        Venue venue = findVenueById(id);
        return entityMapper.toVenueResponse(venue);
    }

    public List<VenueResponse> getAllVenues() {
        List<Venue> venues = venueRepository.findByActiveTrue();
        return entityMapper.toVenueResponseList(venues);
    }

    public List<VenueResponse> getVenuesByCity(Long cityId) {
        // Verify city exists
        cityService.findCityById(cityId);
        List<Venue> venues = venueRepository.findByCityIdAndActiveTrue(cityId);
        return entityMapper.toVenueResponseList(venues);
    }

    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        log.info("Updating venue with ID: {}", id);

        Venue venue = findVenueById(id);
        City city = cityService.findCityById(request.getCityId());

        // Check for duplicate if name or city changed
        if (!venue.getName().equals(request.getName()) ||
                !venue.getCity().getId().equals(request.getCityId())) {
            if (venueRepository.existsByNameAndCityId(request.getName(), request.getCityId())) {
                throw new DuplicateResourceException(
                        String.format("Venue '%s' already exists in city '%s'",
                                request.getName(), city.getName()));
            }
        }

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setPincode(request.getPincode());
        venue.setContactNumber(request.getContactNumber());
        venue.setEmail(request.getEmail());
        venue.setCity(city);

        Venue updatedVenue = venueRepository.save(venue);
        log.info("Venue updated: {}", updatedVenue.getName());

        return entityMapper.toVenueResponse(updatedVenue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        log.info("Deleting venue with ID: {}", id);

        Venue venue = findVenueById(id);
        venue.setActive(false);  // Soft delete
        venueRepository.save(venue);

        log.info("Venue soft deleted: {}", venue.getName());
    }

    @Transactional
    public VenueResponse toggleStatus(Long id) {
        log.info("Toggling status for venue: {}", id);

        Venue venue = findVenueById(id);
        venue.setActive(!venue.isActive());
        Venue updatedVenue = venueRepository.save(venue);

        log.info("Venue {} status changed to: {}", id, updatedVenue.isActive());
        return entityMapper.toVenueResponse(updatedVenue);
    }

    @Transactional
    public void updateSeatCount(Long venueId, int seatCount) {
        Venue venue = findVenueById(venueId);
        venue.setTotalSeats(seatCount);
        venueRepository.save(venue);
        log.debug("Venue {} seat count updated to: {}", venueId, seatCount);
    }

    // ============ Internal Methods ============

    public Venue findVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
    }
}