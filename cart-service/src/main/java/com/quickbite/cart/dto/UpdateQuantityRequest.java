package com.quickbite.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// What Angular sends when customer changes item quantity
@Data
public class UpdateQuantityRequest {

    @NotNull(message = "Cart item ID is required")
    private Long cartItemId;

    // Min 1 — if customer wants 0, they should use remove
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}