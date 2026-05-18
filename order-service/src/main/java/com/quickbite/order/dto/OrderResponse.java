package com.quickbite.order.dto;

import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.enums.OrderStatus;
import com.quickbite.order.enums.PaymentMode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// Clean response sent back to Angular
@Data
public class OrderResponse {

    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private Double totalAmount;
    private Double discount;
    private Double finalAmount;
    private PaymentMode paymentMode;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String deliveryAddress;
    private String specialInstructions;
    private String appliedPromoCode;
    private Integer estimatedDeliveryMin;
    private List<OrderItem> orderItems;
    private String message;

    // Static factory — converts Order entity to OrderResponse DTO
    public static OrderResponse from(Order order) {
        OrderResponse res = new OrderResponse();
        res.setOrderId(order.getOrderId());
        res.setCustomerId(order.getCustomerId());
        res.setRestaurantId(order.getRestaurantId());
        res.setDeliveryAgentId(order.getDeliveryAgentId());
        res.setTotalAmount(order.getTotalAmount());
        res.setDiscount(order.getDiscount());
        res.setFinalAmount(order.getFinalAmount());
        res.setPaymentMode(order.getPaymentMode());
        res.setOrderStatus(order.getOrderStatus());
        res.setOrderDate(order.getOrderDate());
        res.setConfirmedAt(order.getConfirmedAt());
        res.setDeliveredAt(order.getDeliveredAt());
        res.setCancelledAt(order.getCancelledAt());
        res.setDeliveryAddress(order.getDeliveryAddress());
        res.setSpecialInstructions(order.getSpecialInstructions());
        res.setAppliedPromoCode(order.getAppliedPromoCode());
        res.setEstimatedDeliveryMin(order.getEstimatedDeliveryMin());
        res.setOrderItems(order.getOrderItems());
        return res;
    }

    public static OrderResponse from(Order order, String message) {
        OrderResponse res = from(order);
        res.setMessage(message);
        return res;
    }
}