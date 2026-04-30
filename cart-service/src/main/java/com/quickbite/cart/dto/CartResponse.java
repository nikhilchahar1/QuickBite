package com.quickbite.cart.dto;

import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// What we send back to Angular
// We use a response DTO to control exactly what fields are exposed
@Data
public class CartResponse {

    private Long cartId;
    private Long customerId;
    private Long restaurantId;
    private Double totalPrice;
    private Double discount;
    private Double finalPrice;
    private String appliedPromoCode;
    private LocalDateTime updatedAt;
    private List<CartItem> items;
    private int itemCount;
    private String message;

    // Static factory method — converts Cart entity to CartResponse DTO
    // Called: CartResponse.from(cart)
    public static CartResponse from(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setCustomerId(cart.getCustomerId());
        response.setRestaurantId(cart.getRestaurantId());
        response.setTotalPrice(cart.getTotalPrice());
        response.setDiscount(cart.getDiscount());
        response.setFinalPrice(cart.getFinalPrice());
        response.setAppliedPromoCode(cart.getAppliedPromoCode());
        response.setUpdatedAt(cart.getUpdatedAt());
        response.setItems(cart.getItems());
        response.setItemCount(cart.getItems().size());
        return response;
    }

    // Overloaded version with a message
    public static CartResponse from(Cart cart, String message) {
        CartResponse response = from(cart);
        response.setMessage(message);
        return response;
    }
}