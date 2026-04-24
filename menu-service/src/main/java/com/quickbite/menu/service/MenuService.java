package com.quickbite.menu.service;

import com.quickbite.menu.dto.CategoryRequest;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;

import java.util.List;

public interface MenuService {

    MenuCategory addCategory(CategoryRequest request);
    MenuItem addMenuItem(MenuItemRequest request);
    List<MenuCategory> getCategoriesByRestaurant(Long restaurantId);
    List<MenuItem> getItemsByRestaurant(Long restaurantId);
    List<MenuItem> getItemsByCategory(Long categoryId);
    MenuItem getItemById(Long itemId);
    MenuItem updateItem(Long itemId, MenuItemRequest request);
    MenuItem toggleAvailability(Long itemId);
    void deleteItem(Long itemId);
    void deleteCategory(Long categoryId);
    List<MenuItem> searchItems(String name, Long restaurantId);
    List<MenuItem> getVegItems(Long restaurantId);

}