package com.quickbite.restaurant;

import com.quickbite.restaurant.dto.RestaurantRequest;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.repository.RestaurantRepository;
import com.quickbite.restaurant.service.RestaurantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private RestaurantRequest request;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        request = new RestaurantRequest();
        request.setName("Test Restaurant");
        request.setCuisine("Indian");
        request.setAddress("123 Main St");
        request.setCity("Delhi");

        restaurant = new Restaurant();
        restaurant.setRestaurantId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setCuisine("Indian");
        restaurant.setCity("Delhi");
        restaurant.setOwnerId(1L);
        restaurant.setApproved(false);
    }

    @Test
    void registerRestaurant_ShouldSaveAndReturn() {
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        Restaurant result = restaurantService.registerRestaurant(request, 1L);

        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        assertFalse(result.isApproved()); // should not be approved yet
        verify(restaurantRepository, times(1)).save(any());
    }

    @Test
    void approveRestaurant_ShouldSetApprovedTrue() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        Restaurant result = restaurantService.approveRestaurant(1L);

        assertTrue(result.isApproved());
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> restaurantService.getById(99L));
    }
}