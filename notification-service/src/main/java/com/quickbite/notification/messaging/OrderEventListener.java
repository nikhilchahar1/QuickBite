package com.quickbite.notification.messaging;

import com.quickbite.notification.dto.SendNotificationRequest;
import com.quickbite.notification.enums.NotificationChannel;
import com.quickbite.notification.enums.NotificationType;
import com.quickbite.notification.service.EmailService;
import com.quickbite.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    private final EmailService emailService;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleOrderEvent(OrderEvent event) {

        log.info("Received event: {} for order: {}", event.getEventType(), event.getOrderId());

        try {
            // Route to correct notification based on event type
            switch (event.getEventType()) {

                case "order.placed" -> {
                    sendNotification(
                            event.getCustomerId(),
                            event.getOrderId(),
                            NotificationType.ORDER_PLACED,
                            "Order Placed Successfully! 🎉",
                            String.format(
                                    "Your order #%d has been placed. " +
                                            "Total: Rs.%.0f via %s. " +
                                            "We will notify you when confirmed.",
                                    event.getOrderId(),
                                    event.getFinalAmount(),
                                    event.getPaymentMode())
                    );

                if (event.getCustomerEmail() != null && !event.getCustomerEmail().isBlank()) {
                    emailService.sendOrderPlacedEmail(
                            event, event.getCustomerEmail(),
                            event.getCustomerName() != null ? event.getCustomerName() : "Customer"
                        );
                    }
                }

                case "order.confirmed" ->
                        sendNotification(
                                event.getCustomerId(),
                                event.getOrderId(),
                                NotificationType.ORDER_CONFIRMED,
                                "Order Confirmed! 👍",
                                String.format(
                                        "Great news! Restaurant has confirmed " +
                                                "your order #%d and will start " +
                                                "preparing it soon.",
                                        event.getOrderId())
                        );

                case "order.preparing" ->
                        sendNotification(
                                event.getCustomerId(),
                                event.getOrderId(),
                                NotificationType.ORDER_PREPARING,
                                "Your Food is Being Prepared! 👨‍🍳",
                                String.format(
                                        "Your order #%d is being prepared. " +
                                                "Hang tight — it will be ready soon!",
                                        event.getOrderId())
                        );

                case "order.pickedup" ->
                        sendNotification(
                                event.getCustomerId(),
                                event.getOrderId(),
                                NotificationType.ORDER_PICKED_UP,
                                "Order Picked Up! 🛵",
                                String.format(
                                        "Your order #%d has been picked up " +
                                                "by the delivery agent and is on " +
                                                "its way to you!",
                                        event.getOrderId())
                        );

                case "order.delivered" ->
                        sendNotification(
                                event.getCustomerId(),
                                event.getOrderId(),
                                NotificationType.ORDER_DELIVERED,
                                "Order Delivered! 🎊",
                                String.format(
                                        "Your order #%d has been delivered. " +
                                                "Enjoy your meal! Don't forget to " +
                                                "rate your experience.",
                                        event.getOrderId())
                        );

                case "order.cancelled" ->
                        sendNotification(
                                event.getCustomerId(),
                                event.getOrderId(),
                                NotificationType.ORDER_CANCELLED,
                                "Order Cancelled",
                                String.format(
                                        "Your order #%d has been cancelled. " +
                                                "If you paid online, refund will be " +
                                                "processed in 3-5 business days.",
                                        event.getOrderId())
                        );

                default ->
                        log.warn("Unknown event type: {}", event.getEventType());
            }

            log.info("Notification created for event: {}", event.getEventType());

        } catch (Exception e) {
            // Log error but don't throw
            // If we throw, RabbitMQ will redeliver the message
            // causing infinite retry loop
            log.error("Failed to process event: {} for order: {}",
                    event.getEventType(),
                    event.getOrderId(), e);
        }
    }

    // Helper method to avoid code repetition
    private void sendNotification(Long recipientId, Long orderId,
                                  NotificationType type, String title, String message) {

        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientId(recipientId);
        request.setType(type);
        request.setTitle(title);
        request.setMessage(message);
        request.setChannel(NotificationChannel.IN_APP);
        request.setRelatedId(orderId);
        request.setRelatedType("ORDER");

        notificationService.send(request);
    }
}