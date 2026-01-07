package com.bookmyshow.eventservice.entity;

import com.bookmyshow.eventservice.entity.enums.EventStatus;
import com.bookmyshow.eventservice.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(nullable = false)
    private Integer durationMinutes;    // Duration in minutes

    private String language;

    private String genre;               // Action, Comedy, Drama, etc.

    private String certificate;         // U, UA, A, etc. (for movies)

    private LocalDate releaseDate;

    private LocalDate endDate;

    private String posterUrl;

    private String bannerUrl;

    private String trailerUrl;

    private Double rating;              // Average rating

    private Long totalVotes;            // Number of ratings

    @ElementCollection
    @CollectionTable(name = "event_cast", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "cast_member")
    @Builder.Default
    private List<String> cast = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_crew", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "crew_member")
    @Builder.Default
    private List<String> crew = new ArrayList<>();

    // One event has many shows
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Show> shows = new ArrayList<>();

    private boolean active = true;
}