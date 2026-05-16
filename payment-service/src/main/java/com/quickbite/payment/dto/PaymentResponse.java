package com.quickbite.payment.dto;

import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.enums.PaymentMode;
import com.quickbite.payment.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private Long customerId;
    private Double amount;
    private PaymentMode paymentMode;
    private PaymentStatus status;
    private String transactionId;
    private String currency;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String failureReason;
    private String message;

    public static PaymentResponse from(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(p.getPaymentId());
        r.setOrderId(p.getOrderId());
        r.setCustomerId(p.getCustomerId());
        r.setAmount(p.getAmount());
        r.setPaymentMode(p.getPaymentMode());
        r.setStatus(p.getStatus());
        r.setTransactionId(p.getTransactionId());
        r.setCurrency(p.getCurrency());
        r.setPaidAt(p.getPaidAt());
        r.setRefundedAt(p.getRefundedAt());
        r.setFailureReason(p.getFailureReason());
        return r;
    }

    public static PaymentResponse from(Payment p, String message) {
        PaymentResponse r = from(p);
        r.setMessage(message);
        return r;
    }
}