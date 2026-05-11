package com.quickbite.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// What Angular sends when customer submits a review
@Data
public class ReviewRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    private Long agentId;

    // @Min and @Max enforce 1-5 star range
    @NotNull(message = "Food rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer foodRating;

    // Optional delivery rating
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer deliveryRating;

    // Optional written feedback
    private String comment;
}