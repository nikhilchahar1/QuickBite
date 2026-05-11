package com.quickbite.review;

import com.quickbite.review.dto.*;
import com.quickbite.review.entity.Review;
import com.quickbite.review.exception.BadRequestException;
import com.quickbite.review.exception.ResourceNotFoundException;
import com.quickbite.review.repository.ReviewRepository;
import com.quickbite.review.service.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewRequest reviewRequest;
    private ReviewUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {

        review = new Review();
        review.setReviewId(1L);
        review.setOrderId(10L);
        review.setCustomerId(1L);
        review.setRestaurantId(1L);
        review.setAgentId(5L);
        review.setFoodRating(4);
        review.setDeliveryRating(5);
        review.setComment("Great food and fast delivery!");
        review.setReviewDate(LocalDate.now());
        review.setVerified(false);

        reviewRequest = new ReviewRequest();
        reviewRequest.setOrderId(10L);
        reviewRequest.setRestaurantId(1L);
        reviewRequest.setAgentId(5L);
        reviewRequest.setFoodRating(4);
        reviewRequest.setDeliveryRating(5);
        reviewRequest.setComment("Great food!");

        updateRequest = new ReviewUpdateRequest();
        updateRequest.setFoodRating(5);
        updateRequest.setComment("Even better on reflection!");
    }

    // Test 1: addReview saves successfully
    @Test
    void addReview_ShouldSave_WhenOrderNotReviewed() {

        when(reviewRepository.existsByOrderId(10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse result = reviewService.addReview(1L, reviewRequest);

        assertNotNull(result);
        assertEquals(4, result.getFoodRating());
        assertEquals(5, result.getDeliveryRating());
        assertEquals(1L, result.getCustomerId());
        assertEquals("Review submitted successfully", result.getMessage());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    // Test 2: addReview throws when order already reviewed
    @Test
    void addReview_ShouldThrow_WhenOrderAlreadyReviewed() {

        when(reviewRepository.existsByOrderId(10L)).thenReturn(true);

        BadRequestException ex = assertThrows(
                BadRequestException.class, () -> reviewService.addReview(1L, reviewRequest));

        assertEquals("You have already reviewed this order", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    // Test 3: getById returns review when found
    @Test
    void getById_ShouldReturn_WhenFound() {

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse result = reviewService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals(4,  result.getFoodRating());
    }

    // Test 4: getById throws when not found
    @Test
    void getById_ShouldThrow_WhenNotFound() {

        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getById(99L));
    }

    // Test 5: getByRestaurant returns list
    @Test
    void getByRestaurant_ShouldReturnList() {

        when(reviewRepository.findByRestaurantIdOrderByReviewDateDesc(1L)).thenReturn(List.of(review));

        List<ReviewResponse> results = reviewService.getByRestaurant(1L);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getRestaurantId());
    }

    // Test 6: getByOrder returns review
    @Test
    void getByOrder_ShouldReturn_WhenExists() {

        when(reviewRepository.findByOrderId(10L)).thenReturn(Optional.of(review));

        ReviewResponse result = reviewService.getByOrder(10L);

        assertNotNull(result);
        assertEquals(10L, result.getOrderId());
    }

    // Test 7: getByOrder throws when no review for order
    @Test
    void getByOrder_ShouldThrow_WhenNotReviewed() {

        when(reviewRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getByOrder(99L));
    }

    // Test 8: updateReview updates fields correctly
    @Test
    void updateReview_ShouldUpdate_WhenOwner() {

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse result = reviewService.updateReview(1L, 1L, updateRequest);

        assertNotNull(result);
        // foodRating updated to 5
        assertEquals(5, review.getFoodRating());
        // isVerified reset to false after update
        assertFalse(review.isVerified());
        assertEquals("Review updated successfully", result.getMessage());
    }

    // Test 9: updateReview throws when not your review
    @Test
    void updateReview_ShouldThrow_WhenNotOwner() {

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // customerId=99 tries to update review owned by customerId=1
        assertThrows(BadRequestException.class,
                () -> reviewService.updateReview(1L, 99L, updateRequest));

        verify(reviewRepository, never()).save(any());
    }

    // Test 10: deleteReview deletes when found
    @Test
    void deleteReview_ShouldDelete_WhenFound() {

        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).deleteById(1L);
    }

    // Test 11: deleteReview throws when not found
    @Test
    void deleteReview_ShouldThrow_WhenNotFound() {

        when(reviewRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(99L));

        verify(reviewRepository, never()).deleteById(any());
    }

    // Test 12: getAvgFoodRating returns correct average
    @Test
    void getAvgFoodRating_ShouldReturnAverage() {

        when(reviewRepository.avgFoodRatingByRestaurantId(1L)).thenReturn(4.3);
        when(reviewRepository.countByRestaurantId(1L)).thenReturn(10L);

        AverageRatingResponse result = reviewService.getAvgFoodRating(1L);

        assertNotNull(result);
        assertEquals(4.3, result.getAverageRating());
        assertEquals(10L, result.getTotalReviews());
        assertEquals("RESTAURANT", result.getEntityType());
        assertEquals(1L, result.getEntityId());
    }

    // Test 13: getAvgFoodRating returns 0 when no reviews
    @Test
    void getAvgFoodRating_ShouldReturnZero_WhenNoReviews() {

        when(reviewRepository.avgFoodRatingByRestaurantId(1L)).thenReturn(null); // no reviews = null from AVG()
        when(reviewRepository.countByRestaurantId(1L)).thenReturn(0L);

        AverageRatingResponse result = reviewService.getAvgFoodRating(1L);

        assertEquals(0.0, result.getAverageRating());
        assertEquals(0L,  result.getTotalReviews());
    }

    // Test 14: verifyReview sets isVerified true
    @Test
    void verifyReview_ShouldSetVerifiedTrue() {

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse result = reviewService.verifyReview(1L);

        assertTrue(review.isVerified());
        assertEquals("Review verified successfully", result.getMessage());
    }

    // Test 15: getByCustomer returns customer reviews
    @Test
    void getByCustomer_ShouldReturnCustomerReviews() {

        when(reviewRepository.findByCustomerIdOrderByReviewDateDesc(1L)).thenReturn(List.of(review));

        List<ReviewResponse> results = reviewService.getByCustomer(1L);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getCustomerId());
    }
}