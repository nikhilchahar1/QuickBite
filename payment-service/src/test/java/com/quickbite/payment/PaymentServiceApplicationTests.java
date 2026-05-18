package com.quickbite.payment;

import com.quickbite.payment.dto.*;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.entity.Wallet;
import com.quickbite.payment.enums.PaymentMode;
import com.quickbite.payment.enums.PaymentStatus;
import com.quickbite.payment.exception.BadRequestException;
import com.quickbite.payment.exception.ResourceNotFoundException;
import com.quickbite.payment.repository.PaymentRepository;
import com.quickbite.payment.repository.WalletRepository;
import com.quickbite.payment.service.PaymentServiceImpl;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceApplicationTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest cardRequest;
    private PaymentRequest codRequest;
    private PaymentRequest walletRequest;
    private Payment paidPayment;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "currency", "INR");
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "rzp_test_key");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "secret");

        cardRequest = new PaymentRequest();
        cardRequest.setOrderId(1L);
        cardRequest.setAmount(500.0);
        cardRequest.setPaymentMode(PaymentMode.CARD);
        cardRequest.setCardNumber("4111");
        cardRequest.setCardHolder("John Doe");

        codRequest = new PaymentRequest();
        codRequest.setOrderId(2L);
        codRequest.setAmount(300.0);
        codRequest.setPaymentMode(PaymentMode.COD);

        walletRequest = new PaymentRequest();
        walletRequest.setOrderId(3L);
        walletRequest.setAmount(200.0);
        walletRequest.setPaymentMode(PaymentMode.WALLET);

        paidPayment = new Payment();
        paidPayment.setPaymentId(1L);
        paidPayment.setOrderId(1L);
        paidPayment.setCustomerId(1L);
        paidPayment.setAmount(500.0);
        paidPayment.setPaymentMode(PaymentMode.CARD);
        paidPayment.setStatus(PaymentStatus.PAID);
        paidPayment.setTransactionId("CARD-2026-ABCD1234");
        paidPayment.setCurrency("INR");

        wallet = new Wallet();
        wallet.setWalletId(1L);
        wallet.setCustomerId(1L);
        wallet.setBalance(1000.0);
        wallet.setStatements(new ArrayList<>());
    }

    // Test 1: Razorpay order rejects duplicate payment
    @Test
    void createRazorpayOrder_ShouldThrow_WhenDuplicate() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setQuickbiteOrderId(1L);
        request.setAmount(500.0);

        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> paymentService.createRazorpayOrder(
                        1L,
                        request,
                        "John Doe",
                        "john@example.com",
                        "9999999999"));

        verify(paymentRepository, never()).save(any());
    }

    // Test 2: COD payment creates PENDING record
    @Test
    void processCodPayment_ShouldBePending() {
        Payment codPayment = new Payment();
        codPayment.setPaymentId(2L);
        codPayment.setOrderId(2L);
        codPayment.setStatus(PaymentStatus.PENDING);
        codPayment.setPaymentMode(PaymentMode.COD);
        codPayment.setAmount(300.0);
        codPayment.setCurrency("INR");

        when(paymentRepository.existsByOrderId(2L))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(codPayment);

        PaymentResponse result =
                paymentService.processCodPayment(1L, 2L, 300.0);

        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals("Order placed successfully! Pay Rs.300 cash on delivery.",
                result.getMessage());
    }

    // Test 3: Duplicate payment throws exception
    @Test
    void processCodPayment_ShouldThrow_WhenDuplicate() {
        when(paymentRepository.existsByOrderId(1L))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> paymentService.processCodPayment(1L, 1L, 500.0));

        verify(paymentRepository, never()).save(any());
    }

    // Test 4: Wallet payment deducts balance
    @Test
    void payFromWallet_ShouldDeductBalance() {
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);

        Payment walletPayment = new Payment();
        walletPayment.setPaymentId(3L);
        walletPayment.setOrderId(3L);
        walletPayment.setStatus(PaymentStatus.PAID);
        walletPayment.setPaymentMode(PaymentMode.WALLET);
        walletPayment.setAmount(200.0);
        walletPayment.setTransactionId("WALLET-3");
        walletPayment.setCurrency("INR");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(walletPayment);

        PaymentResponse result = paymentService.payFromWallet(1L, 3L, 200.0);

        assertEquals(PaymentStatus.PAID, result.getStatus());
        // Balance should be 1000 - 200 = 800
        assertEquals(800.0, wallet.getBalance());
    }

    // Test 5: Wallet insufficient balance throws
    @Test
    void payFromWallet_ShouldThrow_InsufficientBalance() {
        wallet.setBalance(100.0); // Only Rs.100
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(wallet));

        // Trying to pay Rs.500 with only Rs.100
        assertThrows(BadRequestException.class,
                () -> paymentService.payFromWallet(1L, 1L, 500.0));

        verify(paymentRepository, never()).save(any());
    }

    // Test 6: Top up wallet increases balance
    @Test
    void topUpWallet_ShouldIncreaseBalance() {
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);

        WalletTopUpRequest req = new WalletTopUpRequest();
        req.setAmount(500.0);
        req.setPaymentMode("CARD");

        WalletResponse result =
                paymentService.topUpWallet(1L, req);

        assertNotNull(result);
        // Balance should be 1000 + 500 = 1500
        assertEquals(1500.0, wallet.getBalance());
    }

    // Test 7: Refund payment marks as refunded
    @Test
    void refundPayment_ShouldMarkRefunded() {
        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(paidPayment));
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(paidPayment);

        PaymentResponse result =
                paymentService.refundPayment(1L);

        assertEquals(PaymentStatus.REFUNDED,
                result.getStatus());
        assertTrue(result.getMessage()
                .contains("Refund processed"));
    }

    // Test 8: Refund fails when not PAID
    @Test
    void refundPayment_ShouldThrow_WhenNotPaid() {
        paidPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(paidPayment));

        assertThrows(BadRequestException.class,
                () -> paymentService.refundPayment(1L));
    }

    // Test 9: Get payment by orderId
    @Test
    void getByOrderId_ShouldReturn_WhenFound() {
        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(paidPayment));

        PaymentResponse result =
                paymentService.getByOrderId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(PaymentStatus.PAID, result.getStatus());
    }

    // Test 10: Get payment throws when not found
    @Test
    void getByOrderId_ShouldThrow_WhenNotFound() {
        when(paymentRepository.findByOrderId(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getByOrderId(99L));
    }

    // Test 11: Get wallet creates new if not exists
    @Test
    void getOrCreateWallet_ShouldCreate_WhenNotExists() {
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);

        WalletResponse result =
                paymentService.getOrCreateWallet(1L);

        assertNotNull(result);
        verify(walletRepository, times(1)).save(any());
    }

    // Test 12: Get wallet returns existing
    @Test
    void getOrCreateWallet_ShouldReturn_WhenExists() {
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(wallet));

        WalletResponse result =
                paymentService.getOrCreateWallet(1L);

        assertEquals(1000.0, result.getBalance());
        verify(walletRepository, never()).save(any());
    }

    // Test 13: My payments returns list
    @Test
    void getMyPayments_ShouldReturnList() {
        when(paymentRepository.findByCustomerId(1L))
                .thenReturn(List.of(paidPayment));

        List<PaymentResponse> results =
                paymentService.getMyPayments(1L);

        assertEquals(1, results.size());
        assertEquals(PaymentStatus.PAID,
                results.get(0).getStatus());
    }

    // Test 14: Wallet refund credits back to wallet
    @Test
    void refundPayment_WalletPayment_ShouldCreditWallet() {
        paidPayment.setPaymentMode(PaymentMode.WALLET);

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(paidPayment));
        when(walletRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(paidPayment);

        paymentService.refundPayment(1L);

        // Wallet should be credited Rs.500 back
        assertEquals(1500.0, wallet.getBalance());
    }

    // Test 15: All payments returns list (admin)
    @Test
    void getAllPayments_ShouldReturnAll() {
        when(paymentRepository.findAll())
                .thenReturn(List.of(paidPayment));

        List<PaymentResponse> results = paymentService.getAllPayments();

        assertEquals(1, results.size());
    }
}
