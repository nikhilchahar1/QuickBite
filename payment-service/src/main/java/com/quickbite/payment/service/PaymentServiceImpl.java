package com.quickbite.payment.service;

import com.quickbite.payment.dto.*;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.entity.WalletStatement;
import com.quickbite.payment.enums.PaymentMode;
import com.quickbite.payment.enums.PaymentStatus;
import com.quickbite.payment.enums.WalletTransactionType;
import com.quickbite.payment.exception.BadRequestException;
import com.quickbite.payment.exception.ResourceNotFoundException;
import com.quickbite.payment.repository.PaymentRepository;
import com.quickbite.payment.repository.WalletRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${payment.currency:INR}")
    private String currency;

    // ── createRazorpayOrder ───────────────────────────────────────
    // Step 1 of Razorpay flow
    // Creates an order on Razorpay's server
    // Returns orderId that Angular uses to open checkout popup
    @Override
    public CreateOrderResponse createRazorpayOrder(
            Long customerId,
            CreateOrderRequest request,
            String customerName,
            String customerEmail,
            String customerPhone) {

        // Check no duplicate payment
        if (paymentRepository.existsByOrderId(
                request.getQuickbiteOrderId())) {
            throw new BadRequestException(
                    "Payment already initiated for order: " + request.getQuickbiteOrderId());
        }

        try {
            // Razorpay requires amount in PAISE (1 rupee = 100 paise)
            // Rs.500 → 50000 paise
            int amountInPaise = (int)(request.getAmount() * 100);

            // Build order request for Razorpay API
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt",
                    "quickbite_order_" + request.getQuickbiteOrderId());

            // Notes are optional metadata stored in Razorpay
            JSONObject notes = new JSONObject();
            notes.put("quickbite_order_id", request.getQuickbiteOrderId().toString());
            notes.put("customer_id", customerId.toString());
            orderRequest.put("notes", notes);

            // Call Razorpay API to create order
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            String razorpayOrderId = razorpayOrder.get("id");

            log.info("Razorpay order created: {} for QuickBite order: {}",
                    razorpayOrderId,
                    request.getQuickbiteOrderId());

            // Return all data Angular needs to open popup
            return new CreateOrderResponse(
                    razorpayOrderId,
                    request.getQuickbiteOrderId(),
                    request.getAmount(),
                    currency,
                    razorpayKeyId,    // Angular needs this
                    customerName,
                    customerEmail,
                    customerPhone
            );

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}",
                    e.getMessage());
            throw new BadRequestException(
                    "Payment gateway error. Please try again. "
                            + e.getMessage());
        }
    }

    // ── verifyAndSavePayment ──────────────────────────────────────
    // Step 2 of Razorpay flow — MOST CRITICAL METHOD
    // Verifies the payment signature to prevent fraud
    // If someone forges a payment, signature check will fail
    @Override
    @Transactional
    public PaymentResponse verifyAndSavePayment(
            Long customerId, VerifyPaymentRequest request) {

        // ── SIGNATURE VERIFICATION ─────────────────────────────
        // Razorpay creates a signature using:
        // HMAC-SHA256(razorpayOrderId + "|" + razorpayPaymentId, keySecret)
        // We recreate the same hash and compare
        // If they match → payment is genuine
        // If they don't match → fraud attempt → reject

        boolean signatureValid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!signatureValid) {
            log.error("FRAUD ALERT: Invalid signature for order {}",
                    request.getQuickbiteOrderId());
            throw new BadRequestException(
                    "Payment verification failed. " +
                            "Invalid signature — possible fraud attempt.");
        }

        // Signature valid — save payment record
        Payment payment = new Payment();
        payment.setOrderId(request.getQuickbiteOrderId());
        payment.setCustomerId(customerId);
        payment.setPaymentMode(PaymentMode.CARD);
        // We don't know exact amount here — get from Razorpay
        // For simplicity: set from our records
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setCurrency(currency);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        log.info("Payment verified and saved. " + "Razorpay payment: {} for order: {}",
                request.getRazorpayPaymentId(),
                request.getQuickbiteOrderId());

        return PaymentResponse.from(saved,
                "Payment verified successfully! " +
                        "Transaction ID: "
                        + request.getRazorpayPaymentId());
    }

    // ── processCodPayment ─────────────────────────────────────────
    @Override
    @Transactional
    public PaymentResponse processCodPayment(
            Long customerId,
            Long quickbiteOrderId,
            Double amount) {

        if (paymentRepository.existsByOrderId(quickbiteOrderId)) {
            throw new BadRequestException(
                    "Payment already exists for order: "
                            + quickbiteOrderId);
        }

        Payment payment = new Payment();
        payment.setOrderId(quickbiteOrderId);
        payment.setCustomerId(customerId);
        payment.setAmount(amount);
        payment.setPaymentMode(PaymentMode.COD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCurrency(currency);

        Payment saved = paymentRepository.save(payment);

        return PaymentResponse.from(saved,
                "Order placed successfully! " +
                        "Pay Rs." + amount.intValue()
                        + " cash on delivery.");
    }

    // ── payFromWallet ─────────────────────────────────────────────
    @Override
    @Transactional
    public PaymentResponse payFromWallet(Long customerId,
                                         Long orderId,
                                         Double amount) {

        Wallet wallet = getOrCreateWalletEntity(customerId);

        if (wallet.getBalance() < amount) {
            throw new BadRequestException(
                    String.format(
                            "Insufficient wallet balance. " +
                                    "Available: Rs.%.0f, Required: Rs.%.0f. " +
                                    "Please top up your wallet.",
                            wallet.getBalance(), amount));
        }

        debitWallet(wallet, amount,
                "Payment for order #" + orderId, orderId);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setAmount(amount);
        payment.setPaymentMode(PaymentMode.WALLET);
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId("WALLET-" + orderId);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCurrency(currency);

        Payment saved = paymentRepository.save(payment);

        return PaymentResponse.from(saved,
                "Wallet payment successful! " +
                        "Remaining balance: Rs."
                        + wallet.getBalance().intValue());
    }

    // ── refundPayment ─────────────────────────────────────────────
    @Override
    @Transactional
    public PaymentResponse refundPayment(Long orderId) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment", "orderId", orderId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Cannot refund payment with status: "
                            + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());

        // For WALLET payments — credit back immediately
        if (payment.getPaymentMode() == PaymentMode.WALLET) {
            Wallet wallet = getOrCreateWalletEntity(
                    payment.getCustomerId());
            creditWallet(wallet, payment.getAmount(),
                    "Refund for order #" + orderId, orderId);
        }

        // For CARD/UPI Razorpay payments:
        // In test mode we just mark as refunded
        // In production: call razorpayClient.payments.refund()

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved,
                "Refund processed. Amount will reflect in 3-5 days.");
    }

    // ── getByOrderId ──────────────────────────────────────────────
    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        return PaymentResponse.from(
                paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Payment", "orderId", orderId)));
    }

    // ── getMyPayments
    @Override
    public List<PaymentResponse> getMyPayments(Long customerId) {
        return paymentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // ── getAllPayments
    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    // ── Wallet methods
    @Override
    public WalletResponse getOrCreateWallet(Long customerId) {
        Wallet w = getOrCreateWalletEntity(customerId);
        return new WalletResponse(w.getWalletId(),
                w.getCustomerId(), w.getBalance(),
                "Wallet balance: Rs." + w.getBalance().intValue());
    }

    @Override
    @Transactional
    public WalletResponse topUpWallet(Long customerId, WalletTopUpRequest request) {
        Wallet wallet = getOrCreateWalletEntity(customerId);
        creditWallet(wallet, request.getAmount(),
                "Wallet top-up", null);
        return new WalletResponse(wallet.getWalletId(),
                wallet.getCustomerId(), wallet.getBalance(),
                "Rs." + request.getAmount().intValue() + " added to wallet!");
    }

    @Override
    public List<WalletStatementResponse> getStatements(Long customerId) {
        Wallet wallet = getOrCreateWalletEntity(customerId);
        return wallet.getStatements().stream()
                .map(WalletStatementResponse::from)
                .sorted((a, b) -> b.getCreatedAt()
                        .compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // ── Private Helpers ───────────────────────────────────────────

    private Wallet getOrCreateWalletEntity(Long customerId) {
        return walletRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setCustomerId(customerId);
                    w.setBalance(0.0);
                    return walletRepository.save(w);
                });
    }

    private void creditWallet(Wallet wallet, Double amount, String desc, Long refId) {
        wallet.setBalance(wallet.getBalance() + amount);
        WalletStatement s = new WalletStatement();
        s.setWallet(wallet); s.setType(WalletTransactionType.CREDIT);
        s.setAmount(amount); s.setBalanceAfter(wallet.getBalance());
        s.setDescription(desc); s.setReferenceId(refId);
        wallet.getStatements().add(s);
        walletRepository.save(wallet);
    }

    private void debitWallet(Wallet wallet, Double amount,
                             String desc, Long refId) {
        wallet.setBalance(wallet.getBalance() - amount);
        WalletStatement s = new WalletStatement();
        s.setWallet(wallet); s.setType(WalletTransactionType.DEBIT);
        s.setAmount(amount); s.setBalanceAfter(wallet.getBalance());
        s.setDescription(desc); s.setReferenceId(refId);
        wallet.getStatements().add(s);
        walletRepository.save(wallet);
    }

    // Verifies Razorpay signature to prevent fraud
    // HMAC-SHA256(orderId + "|" + paymentId, keySecret)
    private boolean verifySignature(String razorpayOrderId,
                                    String razorpayPaymentId,
                                    String signature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }


}