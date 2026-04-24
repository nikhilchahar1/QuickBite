package com.quickbite.menu.repository;

import com.quickbite.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByCategoryId(Long categoryId);
    List<MenuItem> findByRestaurantIdAndIsVegTrue(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);
    List<MenuItem> findByNameContainingIgnoreCaseAndRestaurantId(String name, Long restaurantId);

}