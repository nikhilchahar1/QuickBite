package com.quickbite.menu.repository;

import com.quickbite.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);

}