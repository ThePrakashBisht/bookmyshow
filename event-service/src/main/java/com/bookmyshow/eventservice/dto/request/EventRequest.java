package com.bookmyshow.eventservice.dto.request;

import com.bookmyshow.eventservice.entity.enums.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration must not exceed 600 minutes")
    private Integer durationMinutes;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    @Size(max = 10, message = "Certificate must not exceed 10 characters")
    private String certificate;     // U, UA, A (for movies)

    private LocalDate releaseDate;

    private LocalDate endDate;

    @Size(max = 500, message = "Poster URL must not exceed 500 characters")
    private String posterUrl;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;

    @Size(max = 500, message = "Trailer URL must not exceed 500 characters")
    private String trailerUrl;

    @Builder.Default
    private List<String> cast = new ArrayList<>();

    @Builder.Default
    private List<String> crew = new ArrayList<>();
}