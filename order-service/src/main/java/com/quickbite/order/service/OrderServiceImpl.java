package com.quickbite.order.service;

import com.quickbite.order.dto.*;
import com.quickbite.order.entity.*;
import com.quickbite.order.enums.OrderStatus;
import com.quickbite.order.exception.*;
import com.quickbite.order.messaging.*;
import com.quickbite.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {

        // Validate items exist
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with no items");
        }

        // Build the Order entity
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.getRestaurantId());
        order.setTotalAmount(request.getTotalAmount());
        order.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0.0);
        order.setFinalAmount(request.getFinalAmount());
        order.setPaymentMode(request.getPaymentMode());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setAppliedPromoCode(request.getAppliedPromoCode());
        order.setEstimatedDeliveryMin(
                request.getEstimatedDeliveryMin() != null ? request.getEstimatedDeliveryMin() : 30);

        // Snapshot cart items into OrderItems
        for (OrderItemDto dto : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(dto.getMenuItemId());
            orderItem.setItemName(dto.getItemName());
            orderItem.setItemPrice(dto.getItemPrice());
            orderItem.setQuantity(dto.getQuantity());
            orderItem.setSubtotal(dto.getSubtotal());
            orderItem.setCustomization(dto.getCustomization());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Order saved = orderRepository.save(order);

        eventPublisher.publish(OrderEvent.placed(
                saved.getOrderId(),
                customerId,
                saved.getRestaurantId(),
                saved.getPaymentMode(),
                saved.getFinalAmount(),
                request.getCustomerEmail(),
                request.getCustomerName()
        ));

        return OrderResponse.from(saved, "Order placed successfully");
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository
                .findByCustomerIdOrderByOrderDateDesc(customerId)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {



        return orderRepository
                .findByRestaurantIdOrderByOrderDateDesc(restaurantId)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // Only PLACED orders — the restaurant's incoming queue
    @Override
    public List<OrderResponse> getIncomingOrders(Long restaurantId) {
        return orderRepository
                .findByRestaurantIdAndOrderStatus(restaurantId, OrderStatus.PLACED)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateStatusRequest request,
                                      Long actorId, String actorRole) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus current = order.getOrderStatus();
        OrderStatus next = request.getStatus();

        // Validate the transition is legal
        validateStatusTransition(current, next);

        order.setOrderStatus(next);

        // Set timestamps for specific transitions
        switch (next) {
            case CONFIRMED -> {
                order.setConfirmedAt(LocalDateTime.now());
                eventPublisher.publish(OrderEvent.confirmed(
                        orderId,
                        order.getCustomerId(),
                        order.getRestaurantId()
                ));
            }
            case PREPARING -> {
                eventPublisher.publish(OrderEvent.preparing(
                        orderId,
                        order.getCustomerId(),
                        order.getRestaurantId()
                ));
            }
            case PICKED_UP -> {
                eventPublisher.publish(OrderEvent.pickedUp(
                        orderId,
                        order.getCustomerId(),
                        order.getDeliveryAgentId()
                ));
            }
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                eventPublisher.publish(OrderEvent.delivered(
                        orderId,
                        order.getCustomerId(),
                        order.getRestaurantId()
                ));
            }
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                eventPublisher.publish(OrderEvent.cancelled(
                        orderId, order.getCustomerId(), order.getRestaurantId()));
            }
            default -> {}
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved, "Order status updated to " + next);
    }

    // PLACED → CONFIRMED → PREPARING → PICKED_UP → DELIVERED (valid)
    // Any order can be CANCELLED from PLACED or CONFIRMED only
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PLACED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.PICKED_UP;
            case PICKED_UP -> next == OrderStatus.DELIVERED;
            // Terminal states — cannot change
            case DELIVERED, CANCELLED  -> false;
        };

        if (!valid) {
            throw new BadRequestException(String.format("Invalid status transition: %s → %s", current, next));
        }
    }

    @Override
    @Transactional
    public OrderResponse assignDeliveryAgent(Long orderId, Long agentId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Can only assign agent to CONFIRMED or PREPARING orders
        if (order.getOrderStatus() != OrderStatus.CONFIRMED
                && order.getOrderStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException(
                    "Delivery agent can only be assigned to CONFIRMED or PREPARING orders");
        }

        order.setDeliveryAgentId(agentId);
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved, "Delivery agent assigned successfully");
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Only the customer who placed the order can cancel it
        if (!order.getCustomerId().equals(customerId)) {
            throw new BadRequestException("You can only cancel your own orders");
        }

        // Can only cancel PLACED or CONFIRMED orders
        if (order.getOrderStatus() != OrderStatus.PLACED
                && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Order cannot be cancelled after preparation has started. " +
                            "Current status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        eventPublisher.publish(OrderEvent.cancelled(orderId, customerId, order.getRestaurantId()));

        return OrderResponse.from(saved, "Order cancelled successfully");
    }

    // Creates a new order from a past order
    // Copies all items but with a new orderId and PLACED status
    @Override
    @Transactional
    public OrderResponse reorder(Long orderId, Long customerId) {

        // Find the original order
        Order original = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Make sure it belongs to this customer
        if (!original.getCustomerId().equals(customerId)) {
            throw new BadRequestException("You can only reorder your own past orders");
        }

        // Build a new order copying from the original
        Order newOrder = new Order();
        newOrder.setCustomerId(customerId);
        newOrder.setRestaurantId(original.getRestaurantId());
        newOrder.setTotalAmount(original.getTotalAmount());
        newOrder.setDiscount(0.0);     // no promo on reorder
        newOrder.setFinalAmount(original.getTotalAmount());
        newOrder.setPaymentMode(original.getPaymentMode());
        newOrder.setDeliveryAddress(original.getDeliveryAddress());
        newOrder.setSpecialInstructions(original.getSpecialInstructions());
        newOrder.setEstimatedDeliveryMin(original.getEstimatedDeliveryMin());

        // Copy all order items into new order
        for (OrderItem originalItem : original.getOrderItems()) {
            OrderItem newItem = new OrderItem();
            newItem.setMenuItemId(originalItem.getMenuItemId());
            newItem.setItemName(originalItem.getItemName());
            newItem.setItemPrice(originalItem.getItemPrice());
            newItem.setQuantity(originalItem.getQuantity());
            newItem.setSubtotal(originalItem.getSubtotal());
            newItem.setCustomization(originalItem.getCustomization());
            newItem.setOrder(newOrder);
            newOrder.getOrderItems().add(newItem);
        }

        Order saved = orderRepository.save(newOrder);
        return OrderResponse.from(saved, "Reorder placed successfully");
    }

    @Override
    public long getOrderCount(Long restaurantId) {
        return orderRepository.countByRestaurantId(restaurantId);
    }
}