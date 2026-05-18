package com.quickbite.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "reviews",
        indexes = {
                // Fast lookup by restaurant
                @Index(name = "idx_restaurant", columnList = "restaurantId"),
                // Fast lookup by customer
                @Index(name = "idx_customer", columnList = "customerId")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    // orderId must be UNIQUE — one review per order
    @Column(nullable = false, unique = true)
    private Long orderId;

    // Who wrote the review
    @Column(nullable = false)
    private Long customerId;

    // Which restaurant is being reviewed
    @Column(nullable = false)
    private Long restaurantId;

    // Which delivery agent delivered this order
    @Column
    private Long agentId;

    // Food quality rating: 1 to 5 stars
    @Column(nullable = false)
    private Integer foodRating;

    // Delivery experience rating: 1 to 5 stars
    @Column
    private Integer deliveryRating;

    // Optional written comment
    @Column(length = 1000)
    private String comment;

    // When the review was submitted
    @Column(nullable = false)
    private LocalDate reviewDate;

    // Admin can verify genuine reviews
    @Column(nullable = false)
    private boolean isVerified = false;

    @PrePersist
    public void prePersist() {
        this.reviewDate = LocalDate.now();
        this.isVerified = false;
    }
}