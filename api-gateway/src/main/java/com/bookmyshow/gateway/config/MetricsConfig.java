package com.bookmyshow.gateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter requestCounter(MeterRegistry registry) {
        return Counter.builder("gateway.requests.total")
                .description("Total number of requests through gateway")
                .register(registry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("gateway.errors.total")
                .description("Total number of errors in gateway")
                .register(registry);
    }

    @Bean
    public Timer requestTimer(MeterRegistry registry) {
        return Timer.builder("gateway.request.duration")
                .description("Request duration through gateway")
                .register(registry);
    }
}