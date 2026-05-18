package com.quickbite.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.routing.key.pattern}")
    private String routingKeyPattern;

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(queue, true);
    }

    // Binding connects the exchange to the queue
    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(notificationQueue)    // queue
                .to(orderExchange)          // to this exchange
                .with(routingKeyPattern);   // when routing key matches "order.*"
    }

    // JSON converter — converts JSON back to Java objects
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}