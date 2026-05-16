package com.quickbite.payment.service;

import com.quickbite.payment.dto.*;

import java.util.List;

public interface PaymentService {

    // Refund a payment (on order cancellation)
    PaymentResponse refundPayment(Long orderId);

    // Get payment by order
    PaymentResponse getByOrderId(Long orderId);

    // Customer's payment history
    List<PaymentResponse> getMyPayments(Long customerId);

    // Admin: all payments
    List<PaymentResponse> getAllPayments();

    // Wallet operations
    WalletResponse getOrCreateWallet(Long customerId);
    WalletResponse topUpWallet(Long customerId, WalletTopUpRequest request);
    PaymentResponse payFromWallet(Long customerId, Long orderId, Double amount);
    List<WalletStatementResponse> getStatements(Long customerId);


    // ── Razorpay Online Payment (CARD/UPI/NetBanking) ──
    // Step 1: Create Razorpay order — returns order details for popup
    CreateOrderResponse createRazorpayOrder(
            Long customerId,
            CreateOrderRequest request,
            String customerName,
            String customerEmail,
            String customerPhone);

    // Step 2: Verify payment after popup closes
    PaymentResponse verifyAndSavePayment(Long customerId, VerifyPaymentRequest request);

    // ── COD Payment ──
    PaymentResponse processCodPayment(Long customerId, Long quickbiteOrderId, Double amount);

}