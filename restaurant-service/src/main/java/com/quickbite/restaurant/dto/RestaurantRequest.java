package com.quickbite.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// What the frontend sends when registering a restaurant
@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotBlank(message = "Cuisine type is required")
    private String cuisine;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private Double latitude;
    private Double longitude;
    private String phone;
    private Double deliveryRadius;
    private Double minOrderAmount;
    private Integer estimatedDeliveryMin;
    private String imageUrl;
}