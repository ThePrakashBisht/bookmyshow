package com.bookmyshow.gateway.filter;

import com.bookmyshow.gateway.config.RouteConfig;
import com.bookmyshow.gateway.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RouteConfig routeConfig;
    private final ObjectMapper objectMapper;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        // Skip authentication for public paths
        if (routeConfig.isPublicPath(path)) {
            log.debug("Public path, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        // Get Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Check if Authorization header exists
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("Missing Authorization header for protected path: {}", path);
            return onUnauthorized(exchange, "Missing Authorization header");
        }

        // Check Bearer prefix
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Invalid Authorization header format: {}", path);
            return onUnauthorized(exchange, "Invalid Authorization header format. Use 'Bearer <token>'");
        }

        // Extract token
        String token = authHeader.substring(BEARER_PREFIX.length());

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired token for path: {}", path);
            return onUnauthorized(exchange, "Invalid or expired token");
        }

        // Extract user info from token
        String userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractUsername(token);
        List<String> roles = jwtUtil.extractRoles(token);

        // Check admin access
        if (routeConfig.isAdminPath(path) && !roles.contains("ROLE_ADMIN")) {
            log.warn("Access denied to admin path {} for user {}", path, email);
            return onForbidden(exchange, "Access denied. Admin role required.");
        }

        log.debug("Authenticated user: {} with roles: {}", email, roles);

        // Add user info to headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(USER_ID_HEADER, userId != null ? userId : "")
                .header(USER_EMAIL_HEADER, email != null ? email : "")
                .header(USER_ROLES_HEADER, String.join(",", roles))
                .build();

        // Continue with modified request
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Run after LoggingFilter
    }

    private Mono<Void> onUnauthorized(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> onForbidden(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("success", false);
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);
        errorBody.put("path", exchange.getRequest().getURI().getPath());
        errorBody.put("timestamp", LocalDateTime.now().toString());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            String fallback = "{\"success\":false,\"message\":\"" + message + "\"}";
            DataBuffer buffer = response.bufferFactory()
                    .wrap(fallback.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}