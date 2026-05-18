package com.quickbite.menu.controller;

import com.quickbite.menu.dto.CategoryRequest;
import com.quickbite.menu.dto.MenuItemRequest;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import com.quickbite.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/category")
    public ResponseEntity<MenuCategory> addCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addCategory(request));
    }

    @PostMapping("/item")
    public ResponseEntity<MenuItem> addItem(
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addMenuItem(request));
    }

    @GetMapping("/categories/{restaurantId}")
    public ResponseEntity<List<MenuCategory>> getCategories(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getCategoriesByRestaurant(restaurantId));
    }

    @GetMapping("/items/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItem>> getItemsByRestaurant(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getItemsByRestaurant(restaurantId));
    }

    @GetMapping("/items/category/{categoryId}")
    public ResponseEntity<List<MenuItem>> getItemsByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getItemsByCategory(categoryId));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuItem> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.getItemById(itemId));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<MenuItem> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.updateItem(itemId, request));
    }

    @PutMapping("/items/toggle/{itemId}")
    public ResponseEntity<MenuItem> toggleAvailability(@PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.toggleAvailability(itemId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        menuService.deleteItem(itemId);
        return ResponseEntity.ok("Item deleted");
    }

    @DeleteMapping("/category/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        menuService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted");
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<MenuItem>> search(
            @RequestParam String name,
            @RequestParam Long restaurantId) {
        return ResponseEntity.ok(menuService.searchItems(name, restaurantId));
    }

    @GetMapping("/items/veg/{restaurantId}")
    public ResponseEntity<List<MenuItem>> getVegItems(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getVegItems(restaurantId));
    }
}