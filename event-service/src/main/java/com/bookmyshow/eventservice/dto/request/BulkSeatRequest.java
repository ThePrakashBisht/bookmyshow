package com.bookmyshow.eventservice.dto.request;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSeatRequest {

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    // ========== Option 1: Simple Range Mode ==========
    // Use when all rows have same configuration
    // Example: Rows A-E, 10 seats each, all GOLD @ 350

    @Size(max = 2, message = "Row must not exceed 2 characters")
    private String startRow;        // e.g., "A"

    @Size(max = 2, message = "Row must not exceed 2 characters")
    private String endRow;          // e.g., "E"

    @Min(value = 1, message = "Seats per row must be at least 1")
    @Max(value = 50, message = "Seats per row must not exceed 50")
    private Integer seatsPerRow;    // e.g., 10

    private SeatCategory category;  // e.g., GOLD

    @Positive(message = "Base price must be positive")
    private Double basePrice;       // e.g., 350.0

    // ========== Option 2: Multiple Row Configurations ==========
    // Use when different rows have different configurations
    // Example: Row A-B PLATINUM, Row C-E GOLD, Row F-H SILVER

    @Valid
    private List<RowConfiguration> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowConfiguration {

        @NotBlank(message = "Row name is required")
        @Size(max = 5, message = "Row name must not exceed 5 characters")
        private String rowName;         // e.g., "A"

        @NotNull(message = "Start seat is required")
        @Min(value = 1, message = "Start seat must be at least 1")
        private Integer startSeat;      // e.g., 1

        @NotNull(message = "End seat is required")
        @Min(value = 1, message = "End seat must be at least 1")
        private Integer endSeat;        // e.g., 12

        @NotNull(message = "Category is required")
        private SeatCategory category;

        @NotNull(message = "Base price is required")
        @Positive(message = "Base price must be positive")
        private Double basePrice;
    }

    // Helper method to check which mode is being used
    public boolean isSimpleRangeMode() {
        return startRow != null && endRow != null && seatsPerRow != null;
    }

    public boolean isRowConfigurationMode() {
        return rows != null && !rows.isEmpty();
    }
}