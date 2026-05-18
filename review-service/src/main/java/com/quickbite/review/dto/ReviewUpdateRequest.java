package com.quickbite.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

// Customer can only update ratings and comment
// Cannot change orderId or restaurantId
@Data
public class ReviewUpdateRequest {

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer foodRating;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer deliveryRating;

    private String comment;
}