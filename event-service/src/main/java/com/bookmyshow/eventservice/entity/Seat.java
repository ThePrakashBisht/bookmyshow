package com.bookmyshow.eventservice.entity;

import com.bookmyshow.eventservice.entity.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"venue_id", "seat_row", "seat_number"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;     // "A", "B", "C", etc.

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber; // 1, 2, 3, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category;

    @Column(nullable = false)
    private Double basePrice;   // Base price for this seat

    // Many seats belong to one venue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // Get seat label like "A1", "B12"
    public String getSeatLabel() {
        return seatRow + seatNumber;
    }
}