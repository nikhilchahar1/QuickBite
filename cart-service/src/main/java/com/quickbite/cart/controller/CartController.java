package com.quickbite.cart.controller;

import com.quickbite.cart.dto.*;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.service.CartService;
import com.quickbite.cart.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    // ── Helper: extract customerId from JWT
    // All cart operations use this
    // We never trust customerId from request body
    // We always read it from the JWT token
    private Long getCustomerId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    // GET /api/cart
    // Get current customer's cart (or create empty one)
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("Authorization") String authHeader) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.getOrCreateCart(customerId);
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    // POST /api/cart/add
    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AddItemRequest request) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.addItem(customerId, request);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Item added to cart"));
    }

    // DELETE /api/cart/remove/{cartItemId}
    // Remove one item from cart
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long cartItemId) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.removeItem(customerId, cartItemId);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Item removed from cart"));
    }

    // PUT /api/cart/update-quantity
    // Update quantity of a cart item
    @PutMapping("/update-quantity")
    public ResponseEntity<CartResponse> updateQuantity(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateQuantityRequest request) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.updateQuantity(customerId, request);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Quantity updated"));
    }

    // DELETE /api/cart/clear
    // Clear entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(
            @RequestHeader("Authorization") String authHeader) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.clearCart(customerId);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Cart cleared"));
    }

    // PUT /api/cart/switch-restaurant/{restaurantId}
    // Clear cart and set new restaurant
    @PutMapping("/switch-restaurant/{restaurantId}")
    public ResponseEntity<CartResponse> switchRestaurant(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long restaurantId) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.switchRestaurant(customerId, restaurantId);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Switched to new restaurant"));
    }

    // POST /api/cart/promo
    // Apply promo code
    @PostMapping("/promo")
    public ResponseEntity<CartResponse> applyPromo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PromoCodeRequest request) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.applyPromoCode(
                customerId, request.getPromoCode());
        return ResponseEntity.ok(
                CartResponse.from(cart, "Promo code applied successfully"));
    }

    // DELETE /api/cart/promo
    // Remove promo code
    @DeleteMapping("/promo")
    public ResponseEntity<CartResponse> removePromo(
            @RequestHeader("Authorization") String authHeader) {

        Long customerId = getCustomerId(authHeader);
        Cart cart = cartService.removePromoCode(customerId);
        return ResponseEntity.ok(
                CartResponse.from(cart, "Promo code removed"));
    }

    // GET /api/cart/all — Admin only
    @GetMapping("/all")
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }
}