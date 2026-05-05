package com.quickbite.order.service;

import com.quickbite.order.dto.OrderResponse;
import com.quickbite.order.dto.PlaceOrderRequest;
import com.quickbite.order.dto.UpdateStatusRequest;
import com.quickbite.order.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(Long customerId, PlaceOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    // Restaurant's incoming orders
    List<OrderResponse> getOrdersByRestaurant(Long restaurantId);

    // Restaurant's incoming queue (PLACED orders only)
    List<OrderResponse> getIncomingOrders(Long restaurantId);

    // All orders across platform (admin)
    List<OrderResponse> getAllOrders();

    // Update order status
    // PLACED → CONFIRMED → PREPARING → PICKED_UP → DELIVERED
    OrderResponse updateStatus(Long orderId, UpdateStatusRequest request, Long actorId, String actorRole);

    // Assign delivery agent to order
    OrderResponse assignDeliveryAgent(Long orderId, Long agentId);

    // Cancel order
    OrderResponse cancelOrder(Long orderId, Long customerId);

    // Reorder — recreate a past order
    OrderResponse reorder(Long orderId, Long customerId);

    // Count orders for restaurant
    long getOrderCount(Long restaurantId);
}