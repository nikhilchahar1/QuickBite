package com.quickbite.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// A restaurant's menu is divided into categories
@Entity
@Table(name = "menu_categories")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    // Which restaurant this category belongs to
    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    private String description;

    // Order in which categories appear on the menu
    private Integer displayOrder = 0;
}