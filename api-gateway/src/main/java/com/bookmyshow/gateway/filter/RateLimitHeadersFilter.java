package com.bookmyshow.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RateLimitHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();

            // Get rate limit headers from Redis rate limiter
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            String replenishRate = headers.getFirst("X-RateLimit-Replenish-Rate");
            String burstCapacity = headers.getFirst("X-RateLimit-Burst-Capacity");

            // Log rate limit info if headers present
            if (remaining != null) {
                log.debug("Rate limit status - Remaining: {}, Rate: {}/s, Burst: {}",
                        remaining, replenishRate, burstCapacity);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Run last
    }
}