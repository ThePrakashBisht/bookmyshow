package com.bookmyshow.eventservice.entity;

import com.bookmyshow.eventservice.entity.enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show extends BaseEntity {

    @Column(nullable = false)
    private LocalDateTime showTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ShowStatus status = ShowStatus.SCHEDULED;

    @Column(nullable = false)
    private Double basePrice;           // Base ticket price

    private Integer availableSeats;

    private Integer totalSeats;

    // Many shows belong to one event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Many shows happen at one venue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // One show has many show seats (seat status for this show)
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShowSeat> showSeats = new ArrayList<>();

    // Helper to add show seat
    public void addShowSeat(ShowSeat showSeat) {
        showSeats.add(showSeat);
        showSeat.setShow(this);
    }
}