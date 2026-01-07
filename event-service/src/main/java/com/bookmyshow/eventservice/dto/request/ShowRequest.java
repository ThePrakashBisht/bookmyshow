package com.bookmyshow.eventservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    @NotNull(message = "Show time is required")
    @Future(message = "Show time must be in the future")
    private LocalDateTime showTime;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;

    // Price multipliers for different seat categories
    // If not provided, defaults will be used in service layer
    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private Double platinumMultiplier;   // default: 1.5 (50% more than base)

    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private Double goldMultiplier;       // default: 1.2 (20% more than base)

    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private Double silverMultiplier;     // default: 1.0 (same as base)

    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private Double vipMultiplier;        // default: 2.0 (100% more than base)

    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private Double reclinerMultiplier;   // default: 1.8 (80% more than base)

    // Helper methods to get multipliers with defaults
    public Double getPlatinumMultiplier() {
        return platinumMultiplier != null ? platinumMultiplier : 1.5;
    }

    public Double getGoldMultiplier() {
        return goldMultiplier != null ? goldMultiplier : 1.2;
    }

    public Double getSilverMultiplier() {
        return silverMultiplier != null ? silverMultiplier : 1.0;
    }

    public Double getVipMultiplier() {
        return vipMultiplier != null ? vipMultiplier : 2.0;
    }

    public Double getReclinerMultiplier() {
        return reclinerMultiplier != null ? reclinerMultiplier : 1.8;
    }
}