package com.quickbite.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Returned when someone asks:
// "What is the average rating for restaurant is?"
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AverageRatingResponse {

    private Long entityId;       // restaurantId or agentId
    private Double averageRating;
    private Long totalReviews;
    private String entityType;   // "RESTAURANT" or "AGENT"
}