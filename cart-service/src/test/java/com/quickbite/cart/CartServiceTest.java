package com.quickbite.cart;

import com.quickbite.cart.dto.AddItemRequest;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import com.quickbite.cart.exception.BadRequestException;
import com.quickbite.cart.exception.ResourceNotFoundException;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import com.quickbite.cart.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart emptyCart;
    private Cart cartWithItem;
    private CartItem existingItem;
    private AddItemRequest addItemRequest;

    @BeforeEach
    void setUp() {
        // Empty cart for customer 1
        emptyCart = new Cart();
        emptyCart.setCartId(1L);
        emptyCart.setCustomerId(1L);
        emptyCart.setTotalPrice(0.0);
        emptyCart.setDiscount(0.0);
        emptyCart.setFinalPrice(0.0);
        emptyCart.setItems(new ArrayList<>());

        // Existing cart item
        existingItem = new CartItem();
        existingItem.setCartItemId(10L);
        existingItem.setMenuItemId(100L);
        existingItem.setItemName("Paneer Tikka");
        existingItem.setItemPrice(250.0);
        existingItem.setQuantity(2);
        existingItem.setSubtotal(500.0);

        // Cart that already has one item
        cartWithItem = new Cart();
        cartWithItem.setCartId(1L);
        cartWithItem.setCustomerId(1L);
        cartWithItem.setRestaurantId(1L);
        cartWithItem.setTotalPrice(500.0);
        cartWithItem.setDiscount(0.0);
        cartWithItem.setFinalPrice(500.0);
        cartWithItem.setItems(new ArrayList<>());
        cartWithItem.getItems().add(existingItem);
        existingItem.setCart(cartWithItem);

        // Add item request
        addItemRequest = new AddItemRequest();
        addItemRequest.setRestaurantId(1L);
        addItemRequest.setMenuItemId(200L); // different item
        addItemRequest.setItemName("Dal Makhani");
        addItemRequest.setItemPrice(180.0);
        addItemRequest.setQuantity(1);
    }

    // Test 1: getOrCreateCart creates new cart when none exists
    @Test
    void getOrCreateCart_ShouldCreateNewCart_WhenNoneExists() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(emptyCart);

        Cart result = cartService.getOrCreateCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // Test 2: getOrCreateCart returns existing cart
    @Test
    void getOrCreateCart_ShouldReturnExisting_WhenCartExists() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(emptyCart));

        Cart result = cartService.getOrCreateCart(1L);

        assertNotNull(result);
        // save should NOT be called since cart exists
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // Test 3: addItem should add new item to empty cart
    @Test
    void addItem_ShouldAddItem_ToEmptyCart() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(emptyCart));
        when(cartItemRepository.findByCart_CartIdAndMenuItemId(1L, 200L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(emptyCart);

        Cart result = cartService.addItem(1L, addItemRequest);

        assertNotNull(result);
        // RestaurantId should be set from the request
        assertEquals(1L, result.getRestaurantId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // Test 4: addItem should throw when adding from different restaurant
    @Test
    void addItem_ShouldThrow_WhenDifferentRestaurant() {
        // Cart already has items from restaurant 1
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));

        // Request is for restaurant 2 — different!
        addItemRequest.setRestaurantId(2L);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> cartService.addItem(1L, addItemRequest)
        );

        assertTrue(ex.getMessage().contains("different restaurant"));
    }

    // Test 5: addItem increases quantity if item already in cart
    @Test
    void addItem_ShouldIncreaseQuantity_WhenItemAlreadyInCart() {
        // Request for same item already in cart
        addItemRequest.setMenuItemId(100L); // same as existingItem
        addItemRequest.setRestaurantId(1L);
        addItemRequest.setQuantity(1);

        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));
        when(cartItemRepository.findByCart_CartIdAndMenuItemId(1L, 100L))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(existingItem);
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cartWithItem);

        cartService.addItem(1L, addItemRequest);

        // Quantity should have increased from 2 to 3
        assertEquals(3, existingItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    // Test 6: removeItem removes item from cart
    @Test
    void removeItem_ShouldRemoveItem() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cartWithItem);

        Cart result = cartService.removeItem(1L, 10L);

        assertNotNull(result);
        // Item should be gone from list
        assertTrue(result.getItems().isEmpty());
    }

    // Test 7: removeItem throws when item not found
    @Test
    void removeItem_ShouldThrow_WhenItemNotInCart() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));

        // Try to remove item with ID 999 which doesn't exist
        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, 999L));
    }

    // Test 8: clearCart resets everything
    @Test
    void clearCart_ShouldResetCart() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cartWithItem);

        Cart result = cartService.clearCart(1L);

        assertTrue(result.getItems().isEmpty());
        assertNull(result.getRestaurantId());
        assertEquals(0.0, result.getTotalPrice());
        assertNull(result.getAppliedPromoCode());
    }

    // Test 9: applyPromoCode WELCOME10 gives 10% discount
    @Test
    void applyPromoCode_ShouldApplyWelcome10() {
        cartWithItem.setTotalPrice(500.0);
        cartWithItem.setFinalPrice(500.0);

        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cartWithItem);

        Cart result = cartService.applyPromoCode(1L, "WELCOME10");

        // 10% of 500 = 50
        assertEquals(50.0, result.getDiscount());
        assertEquals("WELCOME10", result.getAppliedPromoCode());
    }

    // Test 10: applyPromoCode FLAT50 gives Rs.50 off
    @Test
    void applyPromoCode_ShouldApplyFlat50() {
        cartWithItem.setTotalPrice(300.0);
        cartWithItem.setFinalPrice(300.0);

        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cartWithItem);

        Cart result = cartService.applyPromoCode(1L, "FLAT50");

        assertEquals(50.0, result.getDiscount());
    }

    // Test 11: applyPromoCode throws on invalid code
    @Test
    void applyPromoCode_ShouldThrow_WhenInvalidCode() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(cartWithItem));

        assertThrows(BadRequestException.class,
                () -> cartService.applyPromoCode(1L, "FAKECODE"));
    }

    // Test 12: applyPromoCode throws on empty cart
    @Test
    void applyPromoCode_ShouldThrow_WhenCartEmpty() {
        when(cartRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(emptyCart));

        assertThrows(BadRequestException.class,
                () -> cartService.applyPromoCode(1L, "WELCOME10"));
    }
}