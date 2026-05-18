package com.quickbite.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    // "Indian", "Chinese", "Italian" etc.
    @Column(nullable = false)
    private String cuisine;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    // GPS coordinates for proximity search
    private Double latitude;
    private Double longitude;

    private String phone;

    // Average rating — updated when reviews come in
    private Double avgRating = 0.0;

    private boolean isOpen = true;

    // Has admin approved this restaurant?
    private boolean isApproved = false;

    // How far they deliver (in km)
    private Double deliveryRadius = 5.0;

    // Minimum order amount
    private Double minOrderAmount = 0.0;

    // Estimated delivery time in minutes
    private Integer estimatedDeliveryMin = 30;

    // Restaurant image URL
    private String imageUrl;
}