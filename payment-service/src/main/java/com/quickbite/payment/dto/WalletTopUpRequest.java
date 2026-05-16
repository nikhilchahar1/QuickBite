package com.quickbite.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletTopUpRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Minimum top-up is Rs.1")
    private Double amount;
}