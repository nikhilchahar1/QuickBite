package com.quickbite.review.repository;

import com.quickbite.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews for a restaurant
    List<Review> findByRestaurantIdOrderByReviewDateDesc(Long restaurantId);

    // All reviews by a customer
    List<Review> findByCustomerIdOrderByReviewDateDesc(Long customerId);

    // Find review by orderId
    // as this order already been reviewed?
    Optional<Review> findByOrderId(Long orderId);

    // All reviews for a delivery agent
    List<Review> findByAgentId(Long agentId);

    // Does a review exist for this order?
    boolean existsByOrderId(Long orderId);

    // Count total reviews for a restaurant
    long countByRestaurantId(Long restaurantId);

    // Average food rating for a restaurant
    @Query("SELECT AVG(r.foodRating) FROM Review r " +
            "WHERE r.restaurantId = :restaurantId")
    Double avgFoodRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    // Average delivery rating for an agent
    @Query("SELECT AVG(r.deliveryRating) FROM Review r " +
            "WHERE r.agentId = :agentId " +
            "AND r.deliveryRating IS NOT NULL")
    Double avgDeliveryRatingByAgentId(@Param("agentId") Long agentId);

    // All verified reviews for a restaurant
    List<Review> findByRestaurantIdAndIsVerifiedTrue(Long restaurantId);
}