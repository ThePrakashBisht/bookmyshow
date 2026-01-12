package com.bookmyshow.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return Mono.just(buildFallbackResponse(
                "User Service",
                "User service is currently unavailable. Please try again later."
        ));
    }

    @GetMapping("/event-service")
    public Mono<ResponseEntity<Map<String, Object>>> eventServiceFallback() {
        return Mono.just(buildFallbackResponse(
                "Event Service",
                "Event service is currently unavailable. Please try again later."
        ));
    }

    @GetMapping("/booking-service")
    public Mono<ResponseEntity<Map<String, Object>>> bookingServiceFallback() {
        return Mono.just(buildFallbackResponse(
                "Booking Service",
                "Booking service is currently unavailable. Please try again later."
        ));
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(
            String serviceName, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", message);
        response.put("service", serviceName);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("suggestion", "Please retry after a few moments");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}