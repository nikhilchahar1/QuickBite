package com.quickbite.cart.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// A single item line inside a cart
// e.g. "Paneer Tikka x2 = Rs.500"
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    // Which menu item this is (from menu-service)
    // We store the ID — not the full object (separate DB)
    @Column(nullable = false)
    private Long menuItemId;

    // We snapshot the name and price at the time of adding
    // Why? Because the restaurant owner could change the price later
    // The cart should show the price when the item was ADDED
    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Double itemPrice;

    // How many of this item
    @Column(nullable = false)
    private Integer quantity;

    // Optional customisation note from customer
    // e.g. "less spicy", "extra cheese"
    @Column
    private String customization;

    // Calculated: itemPrice * quantity
    // We store this for quick total calculation
    @Column(nullable = false)
    private Double subtotal;

    // Reference back to the cart
    // @ManyToOne = many CartItems belong to one Cart
    // @JoinColumn = the foreign key column name in cart_items table
    // FetchType.LAZY = don't load the Cart object unless we ask for it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    // Helper method to recalculate subtotal
    // Called whenever quantity or price changes
    public void calculateSubtotal() {
        this.subtotal = this.itemPrice * this.quantity;
    }
}