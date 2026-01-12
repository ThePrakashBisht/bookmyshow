package com.bookmyshow.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2) // High priority
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Gateway error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        HttpStatus status = determineHttpStatus(ex);
        String message = determineMessage(ex, status);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", exchange.getRequest().getURI().getPath());
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing response", e);
            String fallback = "{\"success\":false,\"message\":\"Internal Server Error\"}";
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(fallback.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        }

        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (message.contains("connection refused") || message.contains("connection reset")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (message.contains("timeout")) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (message.contains("rate limit") || message.contains("too many requests")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "The requested service is temporarily unavailable. Please try again later.";
        }
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "The service took too long to respond. Please try again.";
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return "Too many requests. Please slow down and try again.";
        }

        return ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";
    }
}