package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.entity.Restaurant;

import java.util.List;

public interface RestaurantService {

    Restaurant registerRestaurant(RestaurantRequest request, Long ownerId);

    Restaurant getById(Long id);

    List<Restaurant> getByOwner(Long ownerId);

    List<Restaurant> getByCity(String city);

    List<Restaurant> getByCuisine(String cuisine);

    List<Restaurant> searchByName(String name);

    List<Restaurant> getNearby(Double lat, Double lng, Double radius);

    List<Restaurant> getPendingApproval();

    Restaurant approveRestaurant(Long id);

    Restaurant toggleOpen(Long id);

    Restaurant updateRestaurant(Long id, RestaurantRequest request);

    void deleteRestaurant(Long id);

    List<Restaurant> getAllApproved();
}