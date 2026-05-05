package com.quickbite.order.dto;

import com.quickbite.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Restaurant owner or delivery agent updates order status
@Data
public class UpdateStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}