package com.quickbite.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Step 1: Angular asks backend to create a Razorpay order
// Backend returns razorpayOrderId
// Angular uses this to open the Razorpay checkout popup
@Data
public class CreateOrderRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least Rs.1")
    private Double amount;  // in rupees (we convert to paise)

    @NotNull(message = "QuickBite order ID is required")
    private Long quickbiteOrderId;  // our internal order ID

    // Optional note shown in Razorpay popup
    private String description;
}