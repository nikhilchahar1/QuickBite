package com.quickbite.payment.enums;

public enum PaymentMode {
    COD,     // Cash on delivery — no processing needed
    WALLET,  // QuickBite wallet balance
    CARD,    // card payment
    UPI      // UPI payment
}