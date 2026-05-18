package com.quickbite.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// A single food item in a category
@Entity
@Table(name = "menu_items")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    // Optional discounted price
    private Double discountedPrice;

    private String imageUrl;

    // true = vegetarian, false = non-vegetarian
    private boolean isVeg = false;

    // Is this item currently available?
    // Owner can toggle this without deleting the item
    private boolean isAvailable = true;

    private Double rating = 0.0;

    private Integer calories;

    // Tags like "spicy", "bestseller", "new"
    private String tags;
}