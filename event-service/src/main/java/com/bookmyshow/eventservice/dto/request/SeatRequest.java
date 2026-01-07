package com.bookmyshow.eventservice.dto.request;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {

    @NotBlank(message = "Seat row is required")
    @Size(max = 5, message = "Seat row must not exceed 5 characters")
    private String seatRow;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer seatNumber;

    @NotNull(message = "Seat category is required")
    private SeatCategory category;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;
}