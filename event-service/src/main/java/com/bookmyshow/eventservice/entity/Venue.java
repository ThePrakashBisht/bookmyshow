package com.bookmyshow.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue extends BaseEntity {

    @Column(nullable = false)
    private String name;        // "PVR Phoenix"

    @Column(nullable = false)
    private String address;

    private String pincode;

    @Column(nullable = false)
    private Integer totalSeats;

    private String contactNumber;

    private String email;

    private boolean active = true;

    // Many venues belong to one city
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    // One venue has many seats
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    // One venue can have many shows
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Show> shows = new ArrayList<>();

    // Helper method to add seat
    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setVenue(this);
    }

    // Helper method to remove seat
    public void removeSeat(Seat seat) {
        seats.remove(seat);
        seat.setVenue(null);
    }
}