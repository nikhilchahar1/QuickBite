package com.quickbite.payment.entity;

import com.quickbite.payment.enums.PaymentMode;
import com.quickbite.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// One payment record per order
// Stores complete audit trail of every transaction
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    // Which order this payment is for
    // unique = one payment record per order
    @Column(nullable = false, unique = true)
    private Long orderId;

    // Who paid
    @Column(nullable = false)
    private Long customerId;

    // Amount paid
    @Column(nullable = false)
    private Double amount;

    // How they paid
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    // Current status of payment
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // For CARD/UPI: generated transaction ID
    // For WALLET: "WALLET-{orderId}"
    // For COD: null until delivery
    @Column
    private String transactionId;

    // Currency — INR for all
    @Column(nullable = false)
    private String currency = "INR";

    // When payment was processed
    @Column
    private LocalDateTime paidAt;

    // When refund was processed (null if not refunded)
    @Column
    private LocalDateTime refundedAt;

    // Reason for failure
    @Column
    private String failureReason;

    @PrePersist
    public void prePersist() {
        if (this.currency == null) this.currency = "INR";
    }
}