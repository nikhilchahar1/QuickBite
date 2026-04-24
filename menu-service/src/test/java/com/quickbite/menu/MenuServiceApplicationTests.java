package com.quickbite.menu;

import com.quickbite.menu.dto.*;
import com.quickbite.menu.entity.*;
import com.quickbite.menu.repository.*;
import com.quickbite.menu.service.MenuServiceImpl;
import com.quickbite.menu.exception.ResourceNotFoundException;
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
class MenuServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    private CategoryRequest categoryRequest;
    private MenuItemRequest itemRequest;
    private MenuCategory savedCategory;
    private MenuItem savedItem;

    @BeforeEach
    void setUp() {

        // -- Category request (what the controller sends to service)
        categoryRequest = new CategoryRequest();
        categoryRequest.setRestaurantId(1L);
        categoryRequest.setName("Starters");
        categoryRequest.setDescription("Appetisers and starters");
        categoryRequest.setDisplayOrder(1);

        // -- Item request
        itemRequest = new MenuItemRequest();
        itemRequest.setRestaurantId(1L);
        itemRequest.setCategoryId(10L);
        itemRequest.setName("Paneer Tikka");
        itemRequest.setDescription("Grilled cottage cheese");
        itemRequest.setPrice(250.0);
        itemRequest.setVeg(true);
        itemRequest.setCalories(320);
        itemRequest.setTags("spicy,bestseller");

        // -- Saved category (what the DB returns after INSERT)
        savedCategory = new MenuCategory();
        savedCategory.setCategoryId(10L);
        savedCategory.setRestaurantId(1L);
        savedCategory.setName("Starters");
        savedCategory.setDescription("Appetisers and starters");
        savedCategory.setDisplayOrder(1);

        // -- Saved item (what the DB returns after INSERT)
        savedItem = new MenuItem();
        savedItem.setItemId(100L);
        savedItem.setRestaurantId(1L);
        savedItem.setCategoryId(10L);
        savedItem.setName("Paneer Tikka");
        savedItem.setDescription("Grilled cottage cheese");
        savedItem.setPrice(250.0);
        savedItem.setVeg(true);
        savedItem.setAvailable(true);   // default should be true
        savedItem.setCalories(320);
        savedItem.setTags("spicy,bestseller");
    }

    // CATEGORY TESTS

    // Test 1: addCategory should save and return the saved category
    @Test
    void addCategory_ShouldSaveAndReturnCategory() {

        when(categoryRepository.save(any(MenuCategory.class)))
                .thenReturn(savedCategory);

        // ACT
        MenuCategory result = menuService.addCategory(categoryRequest);

        // ASSERT
        assertNotNull(result);
        assertEquals(10L, result.getCategoryId());
        assertEquals("Starters", result.getName());
        assertEquals(1L, result.getRestaurantId());
        assertEquals(1, result.getDisplayOrder());

        // Verify save() was called exactly once
        verify(categoryRepository, times(1)).save(any(MenuCategory.class));
    }

    // Test 2: getCategoriesByRestaurant should return list
    @Test
    void getCategoriesByRestaurant_ShouldReturnList() {

        MenuCategory category2 = new MenuCategory();
        category2.setCategoryId(11L);
        category2.setRestaurantId(1L);
        category2.setName("Main Course");
        category2.setDisplayOrder(2);

        // Mock returns a list of 2 categories
        when(categoryRepository
                .findByRestaurantIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(savedCategory, category2));

        List<MenuCategory> result =
                menuService.getCategoriesByRestaurant(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        // First category should be "Starters" (displayOrder=1)
        assertEquals("Starters", result.get(0).getName());
        // Second should be "Main Course" (displayOrder=2)
        assertEquals("Main Course", result.get(1).getName());

        verify(categoryRepository, times(1))
                .findByRestaurantIdOrderByDisplayOrderAsc(1L);
    }

    // Test 3: deleteCategory should call deleteById once
    @Test
    void deleteCategory_ShouldCallDeleteById() {

        doNothing().when(categoryRepository).deleteById(10L);

        menuService.deleteCategory(10L);

        verify(categoryRepository, times(1)).deleteById(10L);
    }

    // MENU ITEM TESTS

    // Test 4: addMenuItem should save and return item with correct fields
    @Test
    void addMenuItem_ShouldSaveAndReturnItem() {

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenReturn(savedItem);

        MenuItem result = menuService.addMenuItem(itemRequest);

        assertNotNull(result);
        assertEquals(100L,          result.getItemId());
        assertEquals("Paneer Tikka",result.getName());
        assertEquals(250.0,         result.getPrice());
        assertTrue(result.isVeg());
        // isAvailable should be true by default for new items
        assertTrue(result.isAvailable());
        assertEquals("spicy,bestseller", result.getTags());

        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    // Test 5: getItemById should return item when found
    @Test
    void getItemById_ShouldReturnItem_WhenFound() {

        // ARRANGE
        // Optional.of(savedItem) = "item exists in DB"
        when(menuItemRepository.findById(100L))
                .thenReturn(Optional.of(savedItem));

        // ACT
        MenuItem result = menuService.getItemById(100L);

        // ASSERT
        assertNotNull(result);
        assertEquals(100L,          result.getItemId());
        assertEquals("Paneer Tikka",result.getName());
    }

    // Test 6: getItemById should throw exception when item NOT found
    @Test
    void getItemById_ShouldThrowException_WhenNotFound() {
        when(menuItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> menuService.getItemById(999L)
        );

        // Now the message format is:
        // "MenuItem not found with id: 999"
        assertTrue(exception.getMessage()
                .contains("MenuItem"));
    }

    // Test 7: toggleAvailability should flip isAvailable from true to false
    @Test
    void toggleAvailability_ShouldFlipFromTrueToFalse() {

        // ARRANGE
        // savedItem.isAvailable = true (set in setUp)
        when(menuItemRepository.findById(100L))
                .thenReturn(Optional.of(savedItem));

        // After save, return the item with flipped availability
        MenuItem toggledItem = new MenuItem();
        toggledItem.setItemId(100L);
        toggledItem.setAvailable(false); // flipped!
        toggledItem.setName("Paneer Tikka");

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenReturn(toggledItem);

        // ACT
        MenuItem result = menuService.toggleAvailability(100L);

        // ASSERT
        // After toggling from true → should now be false (sold out)
        assertFalse(result.isAvailable());

        verify(menuItemRepository, times(1)).findById(100L);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    // Test 8: toggleAvailability should flip isAvailable from false to true
    @Test
    void toggleAvailability_ShouldFlipFromFalseToTrue() {

        // ARRANGE
        // Start with item that is NOT available (sold out)
        savedItem.setAvailable(false);

        when(menuItemRepository.findById(100L))
                .thenReturn(Optional.of(savedItem));

        MenuItem toggledItem = new MenuItem();
        toggledItem.setItemId(100L);
        toggledItem.setAvailable(true); // flipped back!

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenReturn(toggledItem);

        // ACT
        MenuItem result = menuService.toggleAvailability(100L);

        // ASSERT
        // After toggling from false → should now be true (back in stock)
        assertTrue(result.isAvailable());
    }

    // Test 9: getItemsByRestaurant should return all items
    @Test
    void getItemsByRestaurant_ShouldReturnAllItems() {

        // ARRANGE
        MenuItem item2 = new MenuItem();
        item2.setItemId(101L);
        item2.setName("Dal Makhani");
        item2.setRestaurantId(1L);
        item2.setVeg(true);

        when(menuItemRepository.findByRestaurantId(1L))
                .thenReturn(List.of(savedItem, item2));

        // ACT
        List<MenuItem> result = menuService.getItemsByRestaurant(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Paneer Tikka", result.get(0).getName());
        assertEquals("Dal Makhani",  result.get(1).getName());
    }

    // Test 10: getVegItems should return only veg items
    @Test
    void getVegItems_ShouldReturnOnlyVegItems() {

        // ARRANGE
        // savedItem is veg=true (set in setUp)
        // Create a non-veg item that should NOT be in result
        MenuItem nonVegItem = new MenuItem();
        nonVegItem.setItemId(102L);
        nonVegItem.setName("Chicken Biryani");
        nonVegItem.setVeg(false);

        // The repository method already filters — mock returns only veg items
        when(menuItemRepository.findByRestaurantIdAndIsVegTrue(1L))
                .thenReturn(List.of(savedItem)); // only savedItem (veg=true)

        // ACT
        List<MenuItem> result = menuService.getVegItems(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        // All returned items must be vegetarian
        assertTrue(result.get(0).isVeg());
        assertEquals("Paneer Tikka", result.get(0).getName());
    }

    // Test 11: getItemsByCategory should return items for that category
    @Test
    void getItemsByCategory_ShouldReturnItems() {

        // ARRANGE
        when(menuItemRepository.findByCategoryId(10L))
                .thenReturn(List.of(savedItem));

        // ACT
        List<MenuItem> result = menuService.getItemsByCategory(10L);

        // ASSERT
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getCategoryId());
    }

    // Test 12: searchItems should return matching items
    @Test
    void searchItems_ShouldReturnMatchingItems() {

        // ARRANGE
        when(menuItemRepository
                .findByNameContainingIgnoreCaseAndRestaurantId("paneer", 1L))
                .thenReturn(List.of(savedItem));

        // ACT
        List<MenuItem> result = menuService.searchItems("paneer", 1L);

        // ASSERT
        assertEquals(1, result.size());
        // Name contains "paneer" (case-insensitive)
        assertTrue(result.get(0).getName()
                .toLowerCase().contains("paneer"));
    }

    // Test 13: searchItems should return empty list when no match
    @Test
    void searchItems_ShouldReturnEmptyList_WhenNoMatch() {

        // ARRANGE
        when(menuItemRepository
                .findByNameContainingIgnoreCaseAndRestaurantId("pizza", 1L))
                .thenReturn(List.of()); // empty list

        // ACT
        List<MenuItem> result = menuService.searchItems("pizza", 1L);

        // ASSERT
        assertNotNull(result);
        // Should return empty list, not null or exception
        assertTrue(result.isEmpty());
    }

    // Test 14: deleteItem should call deleteById once
    @Test
    void deleteItem_ShouldCallDeleteById() {

        // ARRANGE
        doNothing().when(menuItemRepository).deleteById(100L);

        // ACT
        menuService.deleteItem(100L);

        // ASSERT
        verify(menuItemRepository, times(1)).deleteById(100L);
    }

    // Test 15: updateItem should update fields and return updated item
    @Test
    void updateItem_ShouldUpdateAndReturnItem() {

        // ARRANGE
        // Existing item in DB
        when(menuItemRepository.findById(100L))
                .thenReturn(Optional.of(savedItem));

        // What we want to update to
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Paneer Tikka Masala");  // changed name
        updateRequest.setPrice(299.0);                  // changed price
        updateRequest.setVeg(true);

        // What the DB returns after save
        MenuItem updatedItem = new MenuItem();
        updatedItem.setItemId(100L);
        updatedItem.setName("Paneer Tikka Masala");
        updatedItem.setPrice(299.0);
        updatedItem.setVeg(true);

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenReturn(updatedItem);

        // ACT
        MenuItem result = menuService.updateItem(100L, updateRequest);

        // ASSERT
        assertNotNull(result);
        assertEquals("Paneer Tikka Masala", result.getName());
        assertEquals(299.0, result.getPrice());

        // Both findById and save should have been called
        verify(menuItemRepository, times(1)).findById(100L);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }
}