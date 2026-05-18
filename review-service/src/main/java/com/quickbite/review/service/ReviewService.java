package com.quickbite.review.service;

import com.quickbite.review.dto.AverageRatingResponse;
import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.dto.ReviewUpdateRequest;

import java.util.List;

public interface ReviewService {

    // Customer submits a review
    ReviewResponse addReview(Long customerId, ReviewRequest request);

    // Get single review
    ReviewResponse getById(Long reviewId);

    // All reviews for a restaurant
    List<ReviewResponse> getByRestaurant(Long restaurantId);

    // All reviews by a customer
    List<ReviewResponse> getByCustomer(Long customerId);

    // Review for a specific order
    ReviewResponse getByOrder(Long orderId);

    // All reviews for a delivery agent
    List<ReviewResponse> getByAgent(Long agentId);

    // Customer updates their review
    ReviewResponse updateReview(Long reviewId, Long customerId, ReviewUpdateRequest request);

    // Admin deletes a review
    void deleteReview(Long reviewId);

    // Average food rating for a restaurant
    AverageRatingResponse getAvgFoodRating(Long restaurantId);

    // Average delivery rating for an agent
    AverageRatingResponse getAvgDeliveryRating(Long agentId);

    // Admin verifies a review
    ReviewResponse verifyReview(Long reviewId);

    // All reviews — admin view
    List<ReviewResponse> getAllReviews();
}