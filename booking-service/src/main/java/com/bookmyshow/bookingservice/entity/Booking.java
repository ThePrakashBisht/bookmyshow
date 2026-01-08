package com.bookmyshow.bookingservice.entity;

import com.bookmyshow.bookingservice.entity.enums.BookingStatus;
import com.bookmyshow.bookingservice.entity.enums.PaymentMethod;
import com.bookmyshow.bookingservice.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String bookingNumber;   // Human readable: BMS-20240115-XXXX

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long venueId;

    // Denormalized for quick access (avoid calling Event Service)
    private String eventTitle;
    private String venueName;
    private LocalDateTime showTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Double subtotal;         // Sum of seat prices

    @Column(nullable = false)
    @Builder.Default
    private Double convenienceFee = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double taxAmount = 0.0;

    @Column(nullable = false)
    private Double totalAmount;      // subtotal + fees + tax

    // Payment details
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentTransactionId;
    private LocalDateTime paymentTime;

    // Lock management
    @Column(nullable = false)
    private LocalDateTime lockExpiresAt;

    // Booking items (seats)
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> bookingItems = new ArrayList<>();

    // User contact (for sending tickets)
    private String userEmail;
    private String userPhone;

    // Cancellation details
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Double refundAmount;

    // Helper method to generate booking number
    @PrePersist
    public void generateBookingNumber() {
        if (this.bookingNumber == null) {
            String datePart = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.bookingNumber = "BMS-" + datePart + "-" + randomPart;
        }
    }

    // Helper method to add booking item
    public void addBookingItem(BookingItem item) {
        bookingItems.add(item);
        item.setBooking(this);
    }

    // Check if lock has expired
    public boolean isLockExpired() {
        return LocalDateTime.now().isAfter(lockExpiresAt);
    }

    // Check if booking can be cancelled
    public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}