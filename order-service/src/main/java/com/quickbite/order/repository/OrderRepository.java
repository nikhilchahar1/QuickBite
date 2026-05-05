package com.quickbite.order.repository;

import com.quickbite.order.entity.Order;
import com.quickbite.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // All orders by a specific customer
    // Sorted newest first — most recent order appears at top
    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    // All orders for a specific restaurant
    List<Order> findByRestaurantIdOrderByOrderDateDesc(Long restaurantId);

    // All orders with a specific status (e.g. all PLACED orders)
    List<Order> findByOrderStatus(OrderStatus status);

    // All active orders for a delivery agent
    // (orders that are PICKED_UP by this agent)
    List<Order> findByDeliveryAgentIdAndOrderStatus(Long deliveryAgentId, OrderStatus status);

    // Orders placed between two dates — for analytics
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // Count total orders for a restaurant
    long countByRestaurantId(Long restaurantId);

    // All orders for a restaurant with specific status
    // e.g. all PLACED orders for restaurant 1 (incoming order queue)
    List<Order> findByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus status);
}