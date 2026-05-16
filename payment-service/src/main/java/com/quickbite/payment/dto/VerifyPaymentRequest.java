package com.quickbite.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Step 2: After customer pays in Razorpay popup
// Angular sends these 3 values to backend for verification
// This is the MOST IMPORTANT step — prevents fraud
@Data
public class VerifyPaymentRequest {

    @NotNull
    private Long quickbiteOrderId;  // our order ID

    // These 3 fields come from Razorpay after payment
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;

    private Double amount;
}