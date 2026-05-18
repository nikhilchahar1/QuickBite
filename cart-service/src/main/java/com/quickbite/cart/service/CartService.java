package com.quickbite.cart.service;

import com.quickbite.cart.dto.*;
import com.quickbite.cart.entity.Cart;

import java.util.List;

public interface CartService {

    // Get or create cart for a customer
    Cart getOrCreateCart(Long customerId);

    // Add item to cart
    Cart addItem(Long customerId, AddItemRequest request);

    // Remove one item from cart
    Cart removeItem(Long customerId, Long cartItemId);

    // Update quantity of an existing cart item
    Cart updateQuantity(Long customerId, UpdateQuantityRequest request);

    // Clear entire cart
    Cart clearCart(Long customerId);

    // Switch restaurant (clears old items, sets new restaurantId)
    Cart switchRestaurant(Long customerId, Long newRestaurantId);

    // Apply a promo code for discount
    Cart applyPromoCode(Long customerId, String promoCode);

    // Remove promo code
    Cart removePromoCode(Long customerId);

    // Admin view all carts
    List<Cart> getAllCarts();
}