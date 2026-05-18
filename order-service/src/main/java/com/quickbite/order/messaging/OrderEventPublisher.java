package com.quickbite.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public void publish(OrderEvent event) {
        try {
            log.info("Publishing event: {} for order: {}", event.getEventType(), event.getOrderId());

            rabbitTemplate.convertAndSend(exchange, event.getEventType(), event);

            log.info("Event published successfully: {}", event.getEventType());

        } catch (Exception e) {
            log.error("Failed to publish event: {}. Order {} still saved successfully.",
                    event.getEventType(), event.getOrderId(), e);
        }
    }
}