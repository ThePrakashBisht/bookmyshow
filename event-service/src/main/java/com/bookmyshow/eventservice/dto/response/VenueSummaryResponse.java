package com.bookmyshow.eventservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueSummaryResponse {

    private Long id;
    private String name;
    private String address;
    private String cityName;
    private Integer totalSeats;
}