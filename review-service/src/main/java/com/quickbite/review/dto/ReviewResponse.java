package com.quickbite.review.dto;

import com.quickbite.review.entity.Review;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewResponse {

    private Long reviewId;
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long agentId;
    private Integer foodRating;
    private Integer deliveryRating;
    private String comment;
    private LocalDate reviewDate;
    private boolean isVerified;
    private String message;

    public static ReviewResponse from(Review review) {
        ReviewResponse res = new ReviewResponse();
        res.setReviewId(review.getReviewId());
        res.setOrderId(review.getOrderId());
        res.setCustomerId(review.getCustomerId());
        res.setRestaurantId(review.getRestaurantId());
        res.setAgentId(review.getAgentId());
        res.setFoodRating(review.getFoodRating());
        res.setDeliveryRating(review.getDeliveryRating());
        res.setComment(review.getComment());
        res.setReviewDate(review.getReviewDate());
        res.setVerified(review.isVerified());
        return res;
    }

    public static ReviewResponse from(Review review, String message) {
        ReviewResponse res = from(review);
        res.setMessage(message);
        return res;
    }
}