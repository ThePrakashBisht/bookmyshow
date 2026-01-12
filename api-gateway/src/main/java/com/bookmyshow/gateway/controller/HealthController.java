package com.bookmyshow.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final WebClient.Builder webClientBuilder;

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> aggregatedHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("gateway", "UP");
        healthStatus.put("timestamp", LocalDateTime.now().toString());

        Mono<String> userServiceHealth = checkServiceHealth("http://localhost:8081/actuator/health");
        Mono<String> eventServiceHealth = checkServiceHealth("http://localhost:8082/actuator/health");
        Mono<String> bookingServiceHealth = checkServiceHealth("http://localhost:8083/actuator/health");

        return Mono.zip(userServiceHealth, eventServiceHealth, bookingServiceHealth)
                .map(tuple -> {
                    Map<String, Object> services = new HashMap<>();
                    services.put("user-service", tuple.getT1());
                    services.put("event-service", tuple.getT2());
                    services.put("booking-service", tuple.getT3());

                    healthStatus.put("services", services);

                    // Overall status
                    boolean allUp = "UP".equals(tuple.getT1()) &&
                            "UP".equals(tuple.getT2()) &&
                            "UP".equals(tuple.getT3());
                    healthStatus.put("status", allUp ? "UP" : "DEGRADED");

                    return ResponseEntity.ok(healthStatus);
                })
                .onErrorReturn(ResponseEntity.ok(Map.of(
                        "gateway", "UP",
                        "status", "DEGRADED",
                        "error", "Could not check all services",
                        "timestamp", LocalDateTime.now().toString()
                )));
    }

    @GetMapping("/ready")
    public Mono<ResponseEntity<Map<String, Object>>> readiness() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "READY",
                "timestamp", LocalDateTime.now().toString()
        )));
    }

    @GetMapping("/live")
    public Mono<ResponseEntity<Map<String, Object>>> liveness() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "ALIVE",
                "timestamp", LocalDateTime.now().toString()
        )));
    }

    private Mono<String> checkServiceHealth(String url) {
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object status = response.get("status");
                    return status != null ? status.toString() : "UNKNOWN";
                })
                .timeout(Duration.ofSeconds(2))
                .onErrorReturn("DOWN");
    }
}