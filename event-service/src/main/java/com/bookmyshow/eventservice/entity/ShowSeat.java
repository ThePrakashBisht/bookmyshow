package com.bookmyshow.eventservice.entity;

import com.bookmyshow.eventservice.entity.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"show_id", "seat_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false)
    private Double price;           // Final price (may differ from base)

    private LocalDateTime lockedAt; // When the seat was locked

    private LocalDateTime lockedUntil; // Lock expiry time

    private Long lockedByUserId;    // User who locked the seat

    private Long bookedByUserId;    // User who booked the seat

    private String bookingId;       // Reference to booking service

    // Many show seats belong to one show
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    // Many show seats reference one seat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // Check if lock has expired
    public boolean isLockExpired() {
        if (lockedUntil == null) return true;
        return LocalDateTime.now().isAfter(lockedUntil);
    }

    // Check if seat can be booked
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE ||
                (status == SeatStatus.LOCKED && isLockExpired());
    }
}