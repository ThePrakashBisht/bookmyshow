package com.bookmyshow.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    // Headers to exclude from logging (sensitive)
    private static final Set<String> EXCLUDED_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generate or get request ID
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        final String finalRequestId = requestId;
        final long startTime = System.currentTimeMillis();

        // Store for later use
        exchange.getAttributes().put(START_TIME_ATTRIBUTE, startTime);
        exchange.getAttributes().put(REQUEST_ID_ATTRIBUTE, requestId);

        // Add request ID to response headers
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        // Log incoming request
        logRequest(request, requestId);

        // Mutate request to include request ID header for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doOnSuccess(aVoid -> logResponse(exchange, finalRequestId, startTime))
                .doOnError(throwable -> logError(exchange, finalRequestId, startTime, throwable));
    }

    private void logRequest(ServerHttpRequest request, String requestId) {
        HttpMethod method = request.getMethod();
        URI uri = request.getURI();
        String clientIp = getClientIp(request);

        log.info("[{}] ──▶ {} {} from {} | Query: {}",
                requestId,
                method,
                uri.getPath(),
                clientIp,
                uri.getQuery() != null ? uri.getQuery() : "none"
        );

        // Log headers in debug mode (excluding sensitive ones)
        if (log.isDebugEnabled()) {
            HttpHeaders headers = request.getHeaders();
            headers.forEach((name, values) -> {
                if (!EXCLUDED_HEADERS.contains(name.toLowerCase())) {
                    log.debug("[{}] Header: {} = {}", requestId, name, values);
                }
            });
        }
    }

    private void logResponse(ServerWebExchange exchange, String requestId, long startTime) {
        ServerHttpResponse response = exchange.getResponse();
        long duration = System.currentTimeMillis() - startTime;

        // Get route info
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : "unknown";

        int statusCode = response.getStatusCode() != null
                ? response.getStatusCode().value()
                : 0;

        String logLevel = statusCode >= 400 ? "WARN" : "INFO";

        if (statusCode >= 400) {
            log.warn("[{}] ◀── {} | {}ms | Route: {}",
                    requestId, statusCode, duration, routeId);
        } else {
            log.info("[{}] ◀── {} | {}ms | Route: {}",
                    requestId, statusCode, duration, routeId);
        }

        // Log slow requests
        if (duration > 2000) {
            log.warn("[{}] ⚠️ Slow request detected: {}ms", requestId, duration);
        }
    }

    private void logError(ServerWebExchange exchange, String requestId,
                          long startTime, Throwable throwable) {
        long duration = System.currentTimeMillis() - startTime;
        String path = exchange.getRequest().getURI().getPath();

        log.error("[{}] ✖ Error processing {} | {}ms | Error: {}",
                requestId, path, duration, throwable.getMessage());
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check for forwarded headers (when behind proxy/load balancer)
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}