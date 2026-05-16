package com.quickbite.payment.entity;

import com.quickbite.payment.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Every credit or debit creates one statement record
// Complete transaction history for customer
@Entity
@Table(name = "wallet_statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statementId;

    // Which wallet this belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // CREDIT = money added, DEBIT = money spent
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTransactionType type;

    @Column(nullable = false)
    private Double amount;

    // Balance after this transaction
    @Column(nullable = false)
    private Double balanceAfter;

    // Description: "Added money", "Order #5 payment", "Refund for order #3"
    @Column(nullable = false)
    private String description;

    // Reference: orderId, topup request id etc.
    @Column
    private Long referenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}