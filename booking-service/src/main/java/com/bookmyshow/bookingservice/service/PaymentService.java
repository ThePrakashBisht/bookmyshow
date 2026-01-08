package com.bookmyshow.bookingservice.service;

import com.bookmyshow.bookingservice.dto.response.PaymentResponse;
import com.bookmyshow.bookingservice.entity.enums.PaymentMethod;
import com.bookmyshow.bookingservice.entity.enums.PaymentStatus;
import com.bookmyshow.bookingservice.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock Payment Service
 * In production, this would integrate with actual payment gateways
 * like Razorpay, Stripe, PayTM, etc.
 */
@Service
@Slf4j
public class PaymentService {

    /**
     * Process payment (Mock implementation)
     */
    public PaymentResponse processPayment(
            String bookingNumber,
            Double amount,
            PaymentMethod paymentMethod,
            String paymentToken,
            boolean simulateSuccess) {

        log.info("Processing payment for booking: {}, amount: {}, method: {}",
                bookingNumber, amount, paymentMethod);

        // Simulate payment processing delay
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate transaction ID
        String transactionId = generateTransactionId(paymentMethod);

        if (simulateSuccess) {
            log.info("Payment successful for booking: {}, transaction: {}",
                    bookingNumber, transactionId);

            return PaymentResponse.builder()
                    .success(true)
                    .message("Payment processed successfully")
                    .bookingNumber(bookingNumber)
                    .transactionId(transactionId)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .paymentMethod(paymentMethod)
                    .amount(amount)
                    .paymentTime(LocalDateTime.now())
                    .build();
        } else {
            log.warn("Payment failed for booking: {}", bookingNumber);

            return PaymentResponse.builder()
                    .success(false)
                    .message("Payment failed. Please try again.")
                    .bookingNumber(bookingNumber)
                    .transactionId(transactionId)
                    .paymentStatus(PaymentStatus.FAILED)
                    .paymentMethod(paymentMethod)
                    .amount(amount)
                    .paymentTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Process refund (Mock implementation)
     */
    public PaymentResponse processRefund(
            String bookingNumber,
            String originalTransactionId,
            Double amount) {

        log.info("Processing refund for booking: {}, amount: {}", bookingNumber, amount);

        // Simulate refund processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String refundTransactionId = "REF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        log.info("Refund successful for booking: {}, refund transaction: {}",
                bookingNumber, refundTransactionId);

        return PaymentResponse.builder()
                .success(true)
                .message("Refund processed successfully")
                .bookingNumber(bookingNumber)
                .transactionId(refundTransactionId)
                .paymentStatus(PaymentStatus.REFUNDED)
                .amount(amount)
                .paymentTime(LocalDateTime.now())
                .build();
    }

    /**
     * Validate payment method
     */
    public void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new PaymentException("Payment method is required");
        }

        // In real implementation, check if payment method is supported
        log.debug("Payment method validated: {}", paymentMethod);
    }

    /**
     * Generate transaction ID based on payment method
     */
    private String generateTransactionId(PaymentMethod paymentMethod) {
        String prefix = switch (paymentMethod) {
            case CREDIT_CARD -> "CC";
            case DEBIT_CARD -> "DC";
            case UPI -> "UPI";
            case NET_BANKING -> "NB";
            case WALLET -> "WAL";
        };

        return prefix + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}