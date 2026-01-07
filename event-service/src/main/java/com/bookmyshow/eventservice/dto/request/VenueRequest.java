package com.bookmyshow.eventservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueRequest {

    @NotBlank(message = "Venue name is required")
    @Size(min = 2, max = 200, message = "Venue name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "City ID is required")
    private Long cityId;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 10000, message = "Total seats must not exceed 10000")
    private Integer totalSeats;

    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    @Email(message = "Invalid email format")
    private String email;
}