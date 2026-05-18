package com.quickbite.cart.service;

import com.quickbite.cart.dto.AddItemRequest;
import com.quickbite.cart.dto.UpdateQuantityRequest;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import com.quickbite.cart.exception.BadRequestException;
import com.quickbite.cart.exception.ResourceNotFoundException;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // Promo codes
    // In a real project these would be in a database table
    // For learning purposes we keep it simple
    private static final String PROMO_WELCOME = "WELCOME10"; // 10% off
    private static final String PROMO_FLAT50   = "FLAT50";   // Rs.50 off

    // If customer has no cart yet, create one
    // This is called first by most operations
    @Override
    public Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    // Create a new empty cart for this customer
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setTotalPrice(0.0);
                    newCart.setDiscount(0.0);
                    newCart.setFinalPrice(0.0);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    @Transactional
    public Cart addItem(Long customerId, AddItemRequest request) {

        Cart cart = getOrCreateCart(customerId);

        // If cart already has items from a DIFFERENT restaurant,
        // reject the request — customer must clear cart first
        if (cart.getRestaurantId() != null
                && !cart.getRestaurantId().equals(request.getRestaurantId())
                && !cart.getItems().isEmpty()) {
            throw new BadRequestException(
                    "Your cart has items from a different restaurant. " +
                            "Please clear your cart before adding from a new restaurant."
            );
        }

        // Set the restaurant if cart was empty
        if (cart.getRestaurantId() == null) {
            cart.setRestaurantId(request.getRestaurantId());
        }

        // Check if item already in cart
        // If customer adds same item again, increase quantity
        // instead of creating a duplicate line
        Optional<CartItem> existingItem = cartItemRepository
                .findByCart_CartIdAndMenuItemId(
                        cart.getCartId(),
                        request.getMenuItemId()
                );

        if (existingItem.isPresent()) {
            // Item already in cart — just increase quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.calculateSubtotal(); // recalculate item subtotal
            cartItemRepository.save(item);
        } else {
            // New item — create a CartItem
            CartItem newItem = new CartItem();
            newItem.setMenuItemId(request.getMenuItemId());
            newItem.setItemName(request.getItemName());
            // Snapshot the price — locked at time of adding
            newItem.setItemPrice(request.getItemPrice());
            newItem.setQuantity(request.getQuantity());
            newItem.setCustomization(request.getCustomization());
            newItem.calculateSubtotal();
            // Link CartItem to this Cart
            newItem.setCart(cart);
            // Add to cart's item list
            cart.getItems().add(newItem);
        }

        // Recalculate cart total
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeItem(Long customerId, Long cartItemId) {

        Cart cart = getOrCreateCart(customerId);

        // Find the item in this cart
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "id", cartItemId));

        // Remove from list — orphanRemoval=true will delete from DB
        cart.getItems().remove(itemToRemove);

        // If cart is now empty, reset restaurantId and promo
        if (cart.getItems().isEmpty()) {
            cart.setRestaurantId(null);
            cart.setAppliedPromoCode(null);
            cart.setDiscount(0.0);
        }

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateQuantity(Long customerId,
                               UpdateQuantityRequest request) {

        Cart cart = getOrCreateCart(customerId);

        // Find the specific cart item
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCartItemId()
                        .equals(request.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "id", request.getCartItemId()));

        // Update quantity and recalculate subtotal
        item.setQuantity(request.getQuantity());
        item.calculateSubtotal();

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart clearCart(Long customerId) {

        Cart cart = getOrCreateCart(customerId);

        // Clear all items — cascade + orphanRemoval deletes from DB
        cart.getItems().clear();
        cart.setRestaurantId(null);
        cart.setTotalPrice(0.0);
        cart.setDiscount(0.0);
        cart.setFinalPrice(0.0);
        cart.setAppliedPromoCode(null);

        return cartRepository.save(cart);
    }

    // Customer wants to order from a different restaurant
    // Clear existing cart and set new restaurantId
    @Override
    @Transactional
    public Cart switchRestaurant(Long customerId, Long newRestaurantId) {
        Cart cart = clearCart(customerId);
        cart.setRestaurantId(newRestaurantId);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart applyPromoCode(Long customerId, String promoCode) {

        Cart cart = getOrCreateCart(customerId);

        // Cart must have items to apply promo
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException(
                    "Cannot apply promo code to an empty cart");
        }

        // Already has a promo?
        if (cart.getAppliedPromoCode() != null) {
            throw new BadRequestException(
                    "A promo code is already applied. Remove it first.");
        }

        double discount = 0.0;

        // Check which promo code was entered
        switch (promoCode.toUpperCase()) {
            case "WELCOME10":
                // 10% of total price
                discount = cart.getTotalPrice() * 0.10;
                break;
            case "FLAT50":
                // Flat Rs.50 off
                discount = 50.0;
                break;
            default:
                throw new BadRequestException(
                        "Invalid promo code: " + promoCode);
        }

        cart.setAppliedPromoCode(promoCode.toUpperCase());
        cart.setDiscount(discount);
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removePromoCode(Long customerId) {

        Cart cart = getOrCreateCart(customerId);

        if (cart.getAppliedPromoCode() == null) {
            throw new BadRequestException("No promo code applied");
        }

        cart.setAppliedPromoCode(null);
        cart.setDiscount(0.0);
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }
}