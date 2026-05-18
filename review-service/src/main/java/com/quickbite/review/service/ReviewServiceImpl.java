package com.quickbite.review.service;

import com.quickbite.review.dto.AverageRatingResponse;
import com.quickbite.review.dto.ReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.dto.ReviewUpdateRequest;
import com.quickbite.review.entity.Review;
import com.quickbite.review.exception.BadRequestException;
import com.quickbite.review.exception.ResourceNotFoundException;
import com.quickbite.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(Long customerId, ReviewRequest request) {

        // One review per order — check unique constraint
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new BadRequestException("You have already reviewed this order");
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setCustomerId(customerId);
        review.setRestaurantId(request.getRestaurantId());
        review.setAgentId(request.getAgentId());
        review.setFoodRating(request.getFoodRating());
        review.setDeliveryRating(request.getDeliveryRating());
        review.setComment(request.getComment());
        // @PrePersist sets reviewDate and isVerified=false

        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved, "Review submitted successfully");
    }

    @Override
    public ReviewResponse getById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return ReviewResponse.from(review);
    }

    @Override
    public List<ReviewResponse> getByRestaurant(Long restaurantId) {
        return reviewRepository
                .findByRestaurantIdOrderByReviewDateDesc(restaurantId)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getByCustomer(Long customerId) {
        return reviewRepository
                .findByCustomerIdOrderByReviewDateDesc(customerId)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponse getByOrder(Long orderId) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "orderId", orderId));
        return ReviewResponse.from(review);
    }

    @Override
    public List<ReviewResponse> getByAgent(Long agentId) {
        return reviewRepository.findByAgentId(agentId)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long customerId, ReviewUpdateRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Only the customer who wrote it can update it
        if (!review.getCustomerId().equals(customerId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        // Update only provided fields
        if (request.getFoodRating() != null)
            review.setFoodRating(request.getFoodRating());

        if (request.getDeliveryRating() != null)
            review.setDeliveryRating(request.getDeliveryRating());

        if (request.getComment() != null)
            review.setComment(request.getComment());

        // Reset verification after update
        // Admin needs to re-verify updated reviews
        review.setVerified(false);

        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved, "Review updated successfully");
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public AverageRatingResponse getAvgFoodRating(Long restaurantId) {

        Double avg = reviewRepository.avgFoodRatingByRestaurantId(restaurantId);

        long count = reviewRepository.countByRestaurantId(restaurantId);

        // If no reviews yet, avg will be null
        return new AverageRatingResponse(
                restaurantId,
                avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                count,
                "RESTAURANT"
        );
    }

    @Override
    public AverageRatingResponse getAvgDeliveryRating(Long agentId) {
        Double avg = reviewRepository.avgDeliveryRatingByAgentId(agentId);
        return new AverageRatingResponse(
                agentId,
                avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                0L,
                "AGENT"
        );
    }

    @Override
    @Transactional
    public ReviewResponse verifyReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setVerified(true);
        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved, "Review verified successfully");
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }
}