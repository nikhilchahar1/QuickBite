package com.quickbite.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Step 1 response: Angular receives this and opens popup
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {

    // Razorpay's order ID — used to open checkout popup
    private String razorpayOrderId;

    // Our internal order ID
    private Long quickbiteOrderId;

    // Amount in rupees (for display)
    private Double amount;

    // Always "INR"
    private String currency;

    // Your Razorpay Key ID — Angular needs this to init SDK
    private String keyId;

    // Prefill data for Razorpay popup
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}