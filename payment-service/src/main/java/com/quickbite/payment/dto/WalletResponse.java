package com.quickbite.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {

    private Long walletId;
    private Long customerId;
    private Double balance;
    private String message;
}