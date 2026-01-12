package com.bookmyshow.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MetricsFilter implements GlobalFilter, Ordered {

    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.nanoTime();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // Record metrics after request completes
                    long duration = System.nanoTime() - startTime;

                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "unknown";

                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    String status = statusCode != null ? String.valueOf(statusCode.value()) : "0";
                    String statusGroup = statusCode != null ?
                            (statusCode.value() / 100) + "xx" : "unknown";

                    // Record request count
                    meterRegistry.counter("gateway_requests_total",
                            "route", routeId,
                            "method", method,
                            "status", status,
                            "status_group", statusGroup
                    ).increment();

                    // Record request duration
                    meterRegistry.timer("gateway_request_duration_seconds",
                            "route", routeId,
                            "method", method
                    ).record(Duration.ofNanos(duration));

                    // Record errors
                    if (statusCode != null && statusCode.isError()) {
                        meterRegistry.counter("gateway_errors_total",
                                "route", routeId,
                                "status", status
                        ).increment();
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2; // After logging filter
    }
}