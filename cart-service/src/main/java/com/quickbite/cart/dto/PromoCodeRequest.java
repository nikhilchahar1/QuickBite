package com.quickbite.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromoCodeRequest {

    @NotBlank(message = "Promo code is required")
    private String promoCode;
}