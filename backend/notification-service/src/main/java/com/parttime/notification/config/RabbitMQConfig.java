package com.parttime.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parttime.exchange";
    public static final String QUEUE_SMS = "parttime.queue.notification.sms";
    public static final String QUEUE_IM = "parttime.queue.notification.im";
    public static final String QUEUE_AUDIT = "parttime.queue.notification.audit";
    public static final String ROUTING_KEY_SMS = "notification.sms";
    public static final String ROUTING_KEY_IM = "notification.im";
    public static final String ROUTING_KEY_AUDIT = "notification.audit";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(QUEUE_SMS, true);
    }

    @Bean
    public Queue imQueue() {
        return new Queue(QUEUE_IM, true);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(QUEUE_AUDIT, true);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(smsQueue).to(exchange).with(ROUTING_KEY_SMS);
    }

    @Bean
    public Binding imBinding(Queue imQueue, TopicExchange exchange) {
        return BindingBuilder.bind(imQueue).to(exchange).with(ROUTING_KEY_IM);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange exchange) {
        return BindingBuilder.bind(auditQueue).to(exchange).with(ROUTING_KEY_AUDIT);
    }
}