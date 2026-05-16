package com.quickbite.payment.controller;

import com.quickbite.payment.dto.*;
import com.quickbite.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private Long uid(String h) { return Long.parseLong(h); }

    // STEP 1: Create Razorpay order
    // Angular calls this FIRST to get razorpayOrderId
    // Then Angular opens Razorpay checkout popup
    @PostMapping("/api/payments/razorpay/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader(value="X-User-Phone",
                    required=false,
                    defaultValue="") String userPhone,
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createRazorpayOrder(
                        uid(userId), request,
                        userName, userEmail, userPhone));
    }

    // STEP 2: Verify payment after Razorpay popup closes
    // Angular sends the 3 values Razorpay gave after payment
    // Backend verifies signature — confirms payment is genuine
    @PostMapping("/api/payments/razorpay/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.verifyAndSavePayment(
                        uid(userId), request));
    }

    // ── COD PAYMENT ───────────────────────────────────────────────
    @PostMapping("/api/payments/cod")
    public ResponseEntity<PaymentResponse> codPayment(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam Long orderId,
            @RequestParam Double amount) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processCodPayment(
                        uid(userId), orderId, amount));
    }

    // ── WALLET PAYMENT ────────────────────────────────────────────
    @PostMapping("/api/payments/wallet/pay")
    public ResponseEntity<PaymentResponse> walletPay(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam Long orderId,
            @RequestParam Double amount) {

        return ResponseEntity.ok(
                paymentService.payFromWallet(
                        uid(userId), orderId, amount));
    }

    // ── REFUND ────────────────────────────────────────────────────
    @PostMapping("/api/payments/refund/{orderId}")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentService.refundPayment(orderId));
    }

    // ── QUERIES ───────────────────────────────────────────────────
    @GetMapping("/api/payments/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentService.getByOrderId(orderId));
    }

    @GetMapping("/api/payments/my")
    public ResponseEntity<List<PaymentResponse>> myPayments(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                paymentService.getMyPayments(uid(userId)));
    }

    @GetMapping("/api/payments/all")
    public ResponseEntity<List<PaymentResponse>> allPayments() {
        return ResponseEntity.ok(
                paymentService.getAllPayments());
    }

    // ── WALLET MANAGEMENT ─────────────────────────────────────────
    @GetMapping("/api/wallet/balance")
    public ResponseEntity<WalletResponse> balance(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                paymentService.getOrCreateWallet(uid(userId)));
    }

    @PostMapping("/api/wallet/add")
    public ResponseEntity<WalletResponse> topUp(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody WalletTopUpRequest request) {
        return ResponseEntity.ok(
                paymentService.topUpWallet(
                        uid(userId), request));
    }

    @GetMapping("/api/wallet/statements")
    public ResponseEntity<List<WalletStatementResponse>> statements(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                paymentService.getStatements(uid(userId)));
    }
}