package com.quickbite.order.controller;

import com.quickbite.order.dto.*;
import com.quickbite.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getCustomerId(@RequestHeader("X-User-Id") String userId) {
        return Long.parseLong(userId);
    }

    // Customer places an order
    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PlaceOrderRequest request) {

        Long customerId = Long.parseLong(userId);
        OrderResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get any order by ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // Customer sees their own order history
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    // Owner sees all orders for their restaurant
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    // Owner sees only new PLACED orders (the incoming queue)
    @GetMapping("/restaurant/{restaurantId}/incoming")
    public ResponseEntity<List<OrderResponse>> getIncoming(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getIncomingOrders(restaurantId));
    }

    // Admin sees all orders on the platform
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Restaurant owner updates: PLACED → CONFIRMED → PREPARING
    // Delivery agent updates: PREPARING → PICKED_UP → DELIVERED
    @PutMapping("/status/{orderId}")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody UpdateStatusRequest request) {

        Long actorId   = Long.parseLong(userId);
        return ResponseEntity.ok(orderService.updateStatus(orderId, request, actorId, role));
    }

    // Assign a delivery agent to an order
    @PutMapping("/assign/{orderId}/{agentId}")
    public ResponseEntity<OrderResponse> assignAgent(
            @PathVariable Long orderId,
            @PathVariable Long agentId) {
        return ResponseEntity.ok(orderService.assignDeliveryAgent(orderId, agentId));
    }

    // Customer cancels their order
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        return ResponseEntity.ok(orderService.cancelOrder(orderId, customerId));
    }

    // Customer reorders from past order
    @PostMapping("/reorder/{orderId}")
    public ResponseEntity<OrderResponse> reorder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String userId) {

        Long customerId = Long.parseLong(userId);
        return ResponseEntity.ok(orderService.reorder(orderId, customerId));
    }

    // Admin: total order count for a restaurant
    @GetMapping("/count/{restaurantId}")
    public ResponseEntity<Long> getCount(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrderCount(restaurantId));
    }
}