package com.quickbite.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// What Angular sends when customer clicks "Add to Cart"
@Data
public class AddItemRequest {

    // Which restaurant this item is from
    // Used to enforce single-restaurant rule
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;

    // We snapshot name and price from menu-service response
    // Angular sends these after fetching item details
    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Item price is required")
    private Double itemPrice;

    // @Min(1) = quantity must be at least 1
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Optional
    private String customization;
}