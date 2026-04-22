package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Get all restaurants by a specific owner
    List<Restaurant> findByOwnerId(Long ownerId);

    // Get all approved restaurants in a city
    List<Restaurant> findByCityAndIsApprovedTrue(String city);

    // Get approved + open restaurants by cuisine
    List<Restaurant> findByCuisineAndIsApprovedTrueAndIsOpenTrue(String cuisine);

    // Search by name containing a keyword (case insensitive)
    List<Restaurant> findByNameContainingIgnoreCaseAndIsApprovedTrue(String name);

    // Get all pending approval restaurants (for admin)
    List<Restaurant> findByIsApprovedFalse();

    // This finds nearby restaurants using Haversine formula
    @Query("SELECT r FROM Restaurant r WHERE r.isApproved = true AND r.isOpen = true " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
            "cos(radians(r.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(r.latitude)))) < :radius")
    List<Restaurant> findNearbyRestaurants(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius
    );
}