package com.quickbite.order.dto;

import com.quickbite.order.enums.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// What Angular sends when customer clicks "Place Order"
@Data
public class PlaceOrderRequest {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String specialInstructions;

    // Total amount from cart (Angular sends this after reading cart-service response)
    @NotNull(message = "Total amount is required")
    private Double totalAmount;

    private Double discount;

    @NotNull(message = "Final amount is required")
    private Double finalAmount;

    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String appliedPromoCode;
    private Integer estimatedDeliveryMin;

    // The cart items to snapshot into order items
    // Angular sends the cart items when placing order
    private java.util.List<OrderItemDto> items;
}