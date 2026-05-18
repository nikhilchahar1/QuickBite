package com.quickbite.order.entity;

import com.quickbite.order.enums.OrderStatus;
import com.quickbite.order.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // Customer placed this order
    @Column(nullable = false)
    private Long customerId;

    // Restaurant this order is from
    @Column(nullable = false)
    private Long restaurantId;

    // Which delivery agent is assigned
    // null = not yet assigned
    @Column
    private Long deliveryAgentId;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double discount;

    @Column(nullable = false)
    private Double finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime deliveredAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private Integer estimatedDeliveryMin;

    @Column(nullable = false)
    private String deliveryAddress;

    // "Leave at Door", "Call when arrived" etc.
    @Column
    private String specialInstructions;

    // The promo code that was applied
    @Column
    private String appliedPromoCode;

    // Immutable snapshot of all ordered items
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.orderDate  = LocalDateTime.now();
        this.orderStatus = OrderStatus.PLACED;
    }
}