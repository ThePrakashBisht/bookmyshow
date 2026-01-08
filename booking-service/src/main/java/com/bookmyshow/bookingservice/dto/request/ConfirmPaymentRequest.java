package com.bookmyshow.bookingservice.dto.request;

import com.bookmyshow.bookingservice.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {

    @NotBlank(message = "Booking number is required")
    private String bookingNumber;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // In real scenario, this would be payment gateway token
    private String paymentToken;

    // For simulation
    private boolean simulateSuccess;
}