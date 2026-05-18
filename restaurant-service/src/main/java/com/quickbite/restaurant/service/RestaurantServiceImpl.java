package com.quickbite.restaurant.service;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.exception.BadRequestException;
import com.quickbite.restaurant.repository.RestaurantRepository;
import com.quickbite.restaurant.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public Restaurant registerRestaurant(RestaurantRequest request, Long ownerId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setPhone(request.getPhone());
        restaurant.setImageUrl(request.getImageUrl());

        if (request.getDeliveryRadius() != null)
            restaurant.setDeliveryRadius(request.getDeliveryRadius());
        if (request.getMinOrderAmount() != null)
            restaurant.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getEstimatedDeliveryMin() != null)
            restaurant.setEstimatedDeliveryMin(request.getEstimatedDeliveryMin());

        // isApproved = false by default, admin must approve
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
    }

    @Override
    public List<Restaurant> getByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Restaurant> getByCity(String city) {
        return restaurantRepository.findByCityAndIsApprovedTrue(city);
    }

    @Override
    public List<Restaurant> getByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineAndIsApprovedTrueAndIsOpenTrue(cuisine);
    }

    @Override
    public List<Restaurant> searchByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCaseAndIsApprovedTrue(name);
    }

    @Override
    public List<Restaurant> getNearby(Double lat, Double lng, Double radius) {
        return restaurantRepository.findNearbyRestaurants(lat, lng, radius);
    }

    @Override
    public List<Restaurant> getPendingApproval() {
        return restaurantRepository.findByIsApprovedFalse();
    }

    @Override
    public Restaurant approveRestaurant(Long id) {
        Restaurant restaurant = getById(id);
        restaurant.setApproved(true);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant toggleOpen(Long id, Long requestingOwnerId) {
        Restaurant restaurant = getById(id);
        if (!restaurant.getOwnerId().equals(requestingOwnerId)) {
            throw new BadRequestException("You can only toggle your own restaurant");
        }
        // Flip the value: if open→close, if closed→open
        restaurant.setOpen(!restaurant.isOpen());
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurant(Long id, RestaurantRequest request, Long requestingOwnerId) {
        Restaurant restaurant = getById(id);

        if (!restaurant.getOwnerId().equals(requestingOwnerId)) {
            throw new BadRequestException("You can only update your own restaurant");
        }
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        if (request.getLatitude() != null)
            restaurant.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            restaurant.setLongitude(request.getLongitude());
        if (request.getPhone() != null)
            restaurant.setPhone(request.getPhone());
        if (request.getImageUrl() != null)
            restaurant.setImageUrl(request.getImageUrl());
        if (request.getDeliveryRadius() != null)
            restaurant.setDeliveryRadius(request.getDeliveryRadius());
        if (request.getMinOrderAmount() != null)
            restaurant.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getEstimatedDeliveryMin() != null)
            restaurant.setEstimatedDeliveryMin(request.getEstimatedDeliveryMin());
        return restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    public List<Restaurant> getAllApproved() {
        return restaurantRepository.findAll()
                .stream()
                .filter(Restaurant::isApproved)
                .toList();
    }
}