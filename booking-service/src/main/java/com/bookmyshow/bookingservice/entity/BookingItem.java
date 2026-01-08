package com.bookmyshow.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Long showSeatId;        // Reference to Event Service's ShowSeat

    @Column(nullable = false)
    private Long seatId;            // Physical seat ID

    // Denormalized seat info (avoid calling Event Service)
    @Column(nullable = false)
    private String seatLabel;       // "A1", "B12"

    @Column(nullable = false)
    private String seatRow;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private String seatCategory;    // "GOLD", "PLATINUM"

    @Column(nullable = false)
    private Double price;           // Price at time of booking
}