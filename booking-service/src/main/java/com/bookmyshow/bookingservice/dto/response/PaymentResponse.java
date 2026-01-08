package com.bookmyshow.bookingservice.dto.response;

import com.bookmyshow.bookingservice.entity.enums.PaymentMethod;
import com.bookmyshow.bookingservice.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private boolean success;
    private String message;
    private String bookingNumber;
    private String transactionId;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Double amount;
    private LocalDateTime paymentTime;
}