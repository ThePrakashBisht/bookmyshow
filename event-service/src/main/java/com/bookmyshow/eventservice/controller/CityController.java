package com.bookmyshow.eventservice.controller;

import com.bookmyshow.eventservice.dto.request.CityRequest;
import com.bookmyshow.eventservice.dto.response.ApiResponse;
import com.bookmyshow.eventservice.dto.response.CityResponse;
import com.bookmyshow.eventservice.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Slf4j
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CityResponse>> createCity(
            @Valid @RequestBody CityRequest request) {
        log.info("Create city request: {}", request.getName());
        CityResponse response = cityService.createCity(request);
        return new ResponseEntity<>(
                ApiResponse.success("City created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        log.info("Get all cities request");
        List<CityResponse> cities = cityService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(@PathVariable Long id) {
        log.info("Get city by ID: {}", id);
        CityResponse response = cityService.getCityById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getCitiesByState(
            @PathVariable String state) {
        log.info("Get cities by state: {}", state);
        List<CityResponse> cities = cityService.getCitiesByState(state);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest request) {
        log.info("Update city: {}", id);
        CityResponse response = cityService.updateCity(id, request);
        return ResponseEntity.ok(ApiResponse.success("City updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        log.info("Delete city: {}", id);
        cityService.deleteCity(id);
        return ResponseEntity.ok(ApiResponse.success("City deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<CityResponse>> toggleCityStatus(@PathVariable Long id) {
        log.info("Toggle city status: {}", id);
        CityResponse response = cityService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.success("City status updated", response));
    }
}