package com.quickbite.cart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// One cart per customer
// A cart belongs to ONE restaurant at a time
// If customer switches restaurant, cart must be cleared first
@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    // Which customer owns this cart
    @Column(nullable = false, unique = true)
    private Long customerId;

    // Which restaurant this cart is from
    // null = cart is empty
    @Column
    private Long restaurantId;

    // Total price of all items in cart
    @Column(nullable = false)
    private Double totalPrice = 0.0;

    // Discount applied by promo code
    @Column(nullable = false)
    private Double discount = 0.0;

    // Final price after discount
    @Column(nullable = false)
    private Double finalPrice = 0.0;

    // The promo code applied (null = no promo)
    @Column
    private String appliedPromoCode;

    // When was cart last updated
    @Column
    private LocalDateTime updatedAt;

    // @OneToMany = one Cart has many CartItems
    // mappedBy = "cart" refers to the 'cart' field in CartItem
    // cascade = ALL means: if we save/delete Cart,
    //           automatically save/delete its CartItems too
    // orphanRemoval = true means: if a CartItem is removed from
    //                 this list, delete it from DB automatically
    @OneToMany(mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    // @PrePersist and @PreUpdate both call this
    // Runs before INSERT and before UPDATE
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper: recalculate total from all cart items
    // Call this every time items change
    public void recalculateTotal() {
        this.totalPrice = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        // Apply discount
        this.finalPrice = this.totalPrice - this.discount;
        // finalPrice should never go below 0
        if (this.finalPrice < 0) this.finalPrice = 0.0;
    }
}