package com.quickbite.cart.controller;

import com.quickbite.cart.dto.*;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ── Helper: extract customerId from JWT
    // All cart operations use this
    // We never trust customerId from request body
    // We always read it from the JWT token
    private Long getCustomerId(@RequestHeader("X-User-Id") String userId) {
        return Long.parseLong(userId);
    }

    // Get current customer's cart (or create empty one)
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.getOrCreateCart(customerId);
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddItemRequest request) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.addItem(customerId, request);
        return ResponseEntity.ok(CartResponse.from(cart, "Item added to cart"));
    }

    // Remove one item from cart
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long cartItemId) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.removeItem(customerId, cartItemId);
        return ResponseEntity.ok(CartResponse.from(cart, "Item removed from cart"));
    }

    // Update quantity of a cart item
    @PutMapping("/update-quantity")
    public ResponseEntity<CartResponse> updateQuantity(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.updateQuantity(customerId, request);
        return ResponseEntity.ok(CartResponse.from(cart, "Quantity updated"));
    }

    // Clear entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.clearCart(customerId);
        return ResponseEntity.ok(CartResponse.from(cart, "Cart cleared"));
    }

    // Clear cart and set new restaurant
    @PutMapping("/switch-restaurant/{restaurantId}")
    public ResponseEntity<CartResponse> switchRestaurant(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long restaurantId) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.switchRestaurant(customerId, restaurantId);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Switched to new restaurant"));
    }

    // Apply promo code
    @PostMapping("/promo")
    public ResponseEntity<CartResponse> applyPromo(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PromoCodeRequest request) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.applyPromoCode(customerId, request.getPromoCode());
        return ResponseEntity.ok(
                CartResponse.from(cart, "Promo code applied successfully"));
    }

    // Remove promo code
    @DeleteMapping("/promo")
    public ResponseEntity<CartResponse> removePromo(
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        Cart cart = cartService.removePromoCode(customerId);
        return ResponseEntity.ok(CartResponse.from(cart, "Promo code removed"));
    }

    // GET /api/cart/all — Admin only
    @GetMapping("/all")
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }
}