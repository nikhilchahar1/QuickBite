package com.quickbite.order;

import com.quickbite.order.dto.*;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.enums.OrderStatus;
import com.quickbite.order.enums.PaymentMode;
import com.quickbite.order.exception.BadRequestException;
import com.quickbite.order.exception.ResourceNotFoundException;
import com.quickbite.order.messaging.OrderEventPublisher;
import com.quickbite.order.repository.OrderRepository;
import com.quickbite.order.service.OrderServiceImpl;
import static org.mockito.Mockito.doNothing;
import com.quickbite.order.messaging.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceApplicationTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order placedOrder;
    private Order confirmedOrder;
    private Order deliveredOrder;
    private PlaceOrderRequest placeRequest;
    private OrderItemDto itemDto;

    @BeforeEach
    void setUp() {

        // Item DTO
        itemDto = new OrderItemDto();
        itemDto.setMenuItemId(1L);
        itemDto.setItemName("Paneer Tikka");
        itemDto.setItemPrice(250.0);
        itemDto.setQuantity(2);
        itemDto.setSubtotal(500.0);

        // Place order request
        placeRequest = new PlaceOrderRequest();
        placeRequest.setRestaurantId(1L);
        placeRequest.setPaymentMode(PaymentMode.COD);
        placeRequest.setDeliveryAddress("123 Main St, Delhi");
        placeRequest.setTotalAmount(500.0);
        placeRequest.setDiscount(0.0);
        placeRequest.setFinalAmount(500.0);
        placeRequest.setEstimatedDeliveryMin(30);
        placeRequest.setItems(List.of(itemDto));

        // A PLACED order entity
        placedOrder = new Order();
        placedOrder.setOrderId(1L);
        placedOrder.setCustomerId(1L);
        placedOrder.setRestaurantId(1L);
        placedOrder.setOrderStatus(OrderStatus.PLACED);
        placedOrder.setPaymentMode(PaymentMode.COD);
        placedOrder.setTotalAmount(500.0);
        placedOrder.setDiscount(0.0);
        placedOrder.setFinalAmount(500.0);
        placedOrder.setDeliveryAddress("123 Main St, Delhi");
        placedOrder.setOrderDate(LocalDateTime.now());
        placedOrder.setOrderItems(new ArrayList<>());

        OrderItem oi = new OrderItem();
        oi.setOrderItemId(10L);
        oi.setMenuItemId(1L);
        oi.setItemName("Paneer Tikka");
        oi.setItemPrice(250.0);
        oi.setQuantity(2);
        oi.setSubtotal(500.0);
        oi.setOrder(placedOrder);
        placedOrder.getOrderItems().add(oi);

        // A CONFIRMED order
        confirmedOrder = new Order();
        confirmedOrder.setOrderId(2L);
        confirmedOrder.setCustomerId(1L);
        confirmedOrder.setOrderStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setTotalAmount(300.0);
        confirmedOrder.setDiscount(0.0);
        confirmedOrder.setFinalAmount(300.0);
        confirmedOrder.setOrderItems(new ArrayList<>());

        // A DELIVERED order
        deliveredOrder = new Order();
        deliveredOrder.setOrderId(3L);
        deliveredOrder.setCustomerId(1L);
        deliveredOrder.setOrderStatus(OrderStatus.DELIVERED);
        deliveredOrder.setTotalAmount(400.0);
        deliveredOrder.setDiscount(0.0);
        deliveredOrder.setFinalAmount(400.0);
        deliveredOrder.setOrderItems(new ArrayList<>());
    }

    // Test 1: placeOrder creates order successfully
    @Test
    void placeOrder_ShouldCreateOrder_WhenValidRequest() {

        doNothing().when(eventPublisher).publish(any(OrderEvent.class));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(placedOrder);

        OrderResponse result = orderService.placeOrder(1L, placeRequest);

        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        assertEquals(PaymentMode.COD, result.getPaymentMode());
        assertEquals("Order placed successfully", result.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // Test 2: placeOrder throws when no items
    @Test
    void placeOrder_ShouldThrow_WhenNoItems() {

        placeRequest.setItems(new ArrayList<>());

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder(1L, placeRequest));
    }

    // Test 3: getOrderById returns order when found
    @Test
    void getOrderById_ShouldReturn_WhenFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(OrderStatus.PLACED, result.getOrderStatus());
    }

    // Test 4: getOrderById throws when not found
    @Test
    void getOrderById_ShouldThrow_WhenNotFound() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(99L));
    }

    // Test 5: updateStatus PLACED → CONFIRMED is valid
    @Test
    void updateStatus_ShouldConfirm_WhenPlaced() {

        doNothing().when(eventPublisher).publish(any(OrderEvent.class));
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(placedOrder);

        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        OrderResponse result = orderService.updateStatus(
                1L, req, 1L, "OWNER");

        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());
    }

    // Test 6: updateStatus PLACED → DELIVERED is INVALID
    @Test
    void updateStatus_ShouldThrow_WhenInvalidTransition() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));

        // Cannot skip from PLACED directly to DELIVERED
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus(OrderStatus.DELIVERED);

        assertThrows(BadRequestException.class,
                () -> orderService.updateStatus(1L, req, 1L, "OWNER"));
    }

    // Test 7: updateStatus DELIVERED → CANCELLED is INVALID (terminal state)
    @Test
    void updateStatus_ShouldThrow_WhenOrderDelivered() {

        when(orderRepository.findById(3L))
                .thenReturn(Optional.of(deliveredOrder));

        UpdateStatusRequest req = new UpdateStatusRequest();
        req.setStatus(OrderStatus.CANCELLED);

        assertThrows(BadRequestException.class,
                () -> orderService.updateStatus(3L, req, 1L, "ADMIN"));
    }

    // Test 8: cancelOrder works when PLACED
    @Test
    void cancelOrder_ShouldCancel_WhenPlaced() {

        doNothing().when(eventPublisher).publish(any(OrderEvent.class));
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(placedOrder);

        OrderResponse result = orderService.cancelOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        assertNotNull(result.getCancelledAt());
        assertEquals("Order cancelled successfully", result.getMessage());
    }

    // Test 9: cancelOrder throws when order not yours
    @Test
    void cancelOrder_ShouldThrow_WhenNotYourOrder() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));

        // customerId=1 placed the order, customerId=99 tries to cancel
        assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(1L, 99L));
    }

    // Test 10: cancelOrder throws when already PREPARING
    @Test
    void cancelOrder_ShouldThrow_WhenPreparing() {

        placedOrder.setOrderStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(placedOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(1L, 1L));
    }

    // Test 11: assignDeliveryAgent works on CONFIRMED order
    @Test
    void assignDeliveryAgent_ShouldAssign_WhenConfirmed() {

        when(orderRepository.findById(2L))
                .thenReturn(Optional.of(confirmedOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(confirmedOrder);

        OrderResponse result =
                orderService.assignDeliveryAgent(2L, 5L);

        assertEquals(5L, result.getDeliveryAgentId());
        assertEquals("Delivery agent assigned successfully",
                result.getMessage());
    }

    // Test 12: assignDeliveryAgent throws when PLACED (not confirmed yet)
    @Test
    void assignDeliveryAgent_ShouldThrow_WhenPlaced() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));

        // Cannot assign agent before restaurant confirms the order
        assertThrows(BadRequestException.class,
                () -> orderService.assignDeliveryAgent(1L, 5L));
    }

    // Test 13: reorder creates new order from past order
    @Test
    void reorder_ShouldCreateNewOrder_FromPastOrder() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(placedOrder);

        OrderResponse result = orderService.reorder(1L, 1L);

        assertNotNull(result);
        assertEquals("Reorder placed successfully", result.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // Test 14: reorder throws when not your order
    @Test
    void reorder_ShouldThrow_WhenNotYourOrder() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(placedOrder));

        assertThrows(BadRequestException.class,
                () -> orderService.reorder(1L, 99L));
    }

    // Test 15: getOrdersByCustomer returns list
    @Test
    void getOrdersByCustomer_ShouldReturnList() {

        when(orderRepository
                .findByCustomerIdOrderByOrderDateDesc(1L))
                .thenReturn(List.of(placedOrder, confirmedOrder));

        List<OrderResponse> result = orderService.getOrdersByCustomer(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}