package com.quickbite.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotNull
    private Long restaurantId;

    @NotNull
    private Long categoryId;

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private Double price;

    private Double discountedPrice;
    private String imageUrl;
    private boolean isVeg;
    private Integer calories;
    private String tags;
}