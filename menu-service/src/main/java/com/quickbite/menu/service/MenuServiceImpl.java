package com.quickbite.menu.service;

import com.quickbite.menu.dto.*;
import com.quickbite.menu.entity.*;
import com.quickbite.menu.repository.*;
import com.quickbite.menu.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public MenuCategory addCategory(CategoryRequest request) {
        MenuCategory category = new MenuCategory();
        category.setRestaurantId(request.getRestaurantId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null)
            category.setDisplayOrder(request.getDisplayOrder());
        return categoryRepository.save(category);
    }

    @Override
    public MenuItem addMenuItem(MenuItemRequest request) {
        MenuItem item = new MenuItem();
        item.setRestaurantId(request.getRestaurantId());
        item.setCategoryId(request.getCategoryId());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setDiscountedPrice(request.getDiscountedPrice());
        item.setImageUrl(request.getImageUrl());
        item.setVeg(request.isVeg());
        item.setCalories(request.getCalories());
        item.setTags(request.getTags());
        return menuItemRepository.save(item);
    }

    @Override
    public List<MenuCategory> getCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository
                .findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
    }

    @Override
    public List<MenuItem> getItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<MenuItem> getItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    public MenuItem getItemById(Long itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
    }

    @Override
    public MenuItem updateItem(Long itemId, MenuItemRequest request) {
        MenuItem item = getItemById(itemId);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        if (request.getDiscountedPrice() != null)
            item.setDiscountedPrice(request.getDiscountedPrice());
        if (request.getImageUrl() != null)
            item.setImageUrl(request.getImageUrl());
        item.setVeg(request.isVeg());
        if (request.getCalories() != null)
            item.setCalories(request.getCalories());
        if (request.getTags() != null)
            item.setTags(request.getTags());
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem toggleAvailability(Long itemId) {
        MenuItem item = getItemById(itemId);
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }

    @Override
    public void deleteItem(Long itemId) {
        menuItemRepository.deleteById(itemId);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<MenuItem> searchItems(String name, Long restaurantId) {
        return menuItemRepository
                .findByNameContainingIgnoreCaseAndRestaurantId(name, restaurantId);
    }

    @Override
    public List<MenuItem> getVegItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsVegTrue(restaurantId);
    }
}