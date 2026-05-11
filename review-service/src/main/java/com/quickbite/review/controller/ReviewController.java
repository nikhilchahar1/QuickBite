package com.quickbite.review.controller;

import com.quickbite.review.dto.AverageRatingResponse;
import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.dto.ReviewUpdateRequest;
import com.quickbite.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    private Long getCustomerId(@RequestHeader("X-User-Id") String userId) {
        return Long.parseLong(userId);
    }

    // Customer submits a review after order delivered
    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReviewRequest request) {

        Long customerId = getCustomerId(userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.addReview(customerId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    // All reviews for a restaurant — shown on restaurant page
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getByRestaurant(restaurantId));
    }

    // Customer sees their own reviews
    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @RequestHeader("X-User-Id") String userId) {
        Long customerId = getCustomerId(userId);
        return ResponseEntity.ok(reviewService.getByCustomer(customerId));
    }

    // Check if an order has been reviewed
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(reviewService.getByOrder(orderId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ReviewResponse>> getByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewService.getByAgent(agentId));
    }

    // Customer updates their review
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReviewUpdateRequest request) {

        Long customerId = getCustomerId(userId);
        return ResponseEntity.ok(reviewService.updateReview(id, customerId, request));
    }

    // Admin deletes inappropriate review
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }

    // Average food rating for a restaurant
    @GetMapping("/avg/restaurant/{restaurantId}")
    public ResponseEntity<AverageRatingResponse> getAvgFood(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getAvgFoodRating(restaurantId));
    }

    // Average delivery rating for an agent
    @GetMapping("/avg/agent/{agentId}")
    public ResponseEntity<AverageRatingResponse> getAvgDelivery(@PathVariable Long agentId) {
        return ResponseEntity.ok(reviewService.getAvgDeliveryRating(agentId));
    }

    // Admin verifies a review
    @PutMapping("/verify/{id}")
    public ResponseEntity<ReviewResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.verifyReview(id));
    }

    // Admin sees all reviews
    @GetMapping("/all")
    public ResponseEntity<List<ReviewResponse>> getAll() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}