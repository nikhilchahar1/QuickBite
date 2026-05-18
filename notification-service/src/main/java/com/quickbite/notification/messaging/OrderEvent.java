package com.quickbite.notification.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    private String eventType;
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private String orderStatus;
    private String paymentMode;
    private Double finalAmount;
    private String deliveryAddress;
    private LocalDateTime eventTimestamp;
    private String customerEmail;
    private String customerName;

}