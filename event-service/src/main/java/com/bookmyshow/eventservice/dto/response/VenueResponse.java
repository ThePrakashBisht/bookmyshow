package com.bookmyshow.eventservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {

    private Long id;
    private String name;
    private String address;
    private String pincode;
    private Integer totalSeats;
    private String contactNumber;
    private String email;
    private boolean active;

    // City info (nested)
    private CityResponse city;
}