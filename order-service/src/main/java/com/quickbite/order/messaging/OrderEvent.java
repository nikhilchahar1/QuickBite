package com.quickbite.order.messaging;

import com.quickbite.order.enums.OrderStatus;
import com.quickbite.order.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// This is the MESSAGE that travels through RabbitMQ
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    // eventType "order.placed", "order.confirmed"
    private String eventType;

    // Details needed for notification service to create notification
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private OrderStatus orderStatus;
    private PaymentMode paymentMode;
    private Double finalAmount;
    private String deliveryAddress;
    private LocalDateTime eventTimestamp;
    private String customerEmail;
    private String customerName;

    // Factory methods — convenient ways to create events for each order lifecycle stage

    public static OrderEvent placed(Long orderId, Long customerId, Long restaurantId,
                                    PaymentMode paymentMode, Double finalAmount, String customerEmail,
                                    String customerName) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.placed");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setRestaurantId(restaurantId);
        event.setOrderStatus(OrderStatus.PLACED);
        event.setPaymentMode(paymentMode);
        event.setFinalAmount(finalAmount);
        event.setCustomerEmail(customerEmail);
        event.setCustomerName(customerName);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }

    public static OrderEvent confirmed(Long orderId, Long customerId, Long restaurantId) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.confirmed");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setRestaurantId(restaurantId);
        event.setOrderStatus(OrderStatus.CONFIRMED);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }

    public static OrderEvent preparing(Long orderId, Long customerId, Long restaurantId) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.preparing");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setRestaurantId(restaurantId);
        event.setOrderStatus(OrderStatus.PREPARING);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }

    public static OrderEvent pickedUp(Long orderId, Long customerId, Long deliveryAgentId) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.pickedup");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setDeliveryAgentId(deliveryAgentId);
        event.setOrderStatus(OrderStatus.PICKED_UP);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }

    public static OrderEvent delivered(Long orderId, Long customerId, Long restaurantId) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.delivered");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setRestaurantId(restaurantId);
        event.setOrderStatus(OrderStatus.DELIVERED);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }

    public static OrderEvent cancelled(Long orderId, Long customerId, Long restaurantId) {
        OrderEvent event = new OrderEvent();
        event.setEventType("order.cancelled");
        event.setOrderId(orderId);
        event.setCustomerId(customerId);
        event.setRestaurantId(restaurantId);
        event.setOrderStatus(OrderStatus.CANCELLED);
        event.setEventTimestamp(LocalDateTime.now());
        return event;
    }
}