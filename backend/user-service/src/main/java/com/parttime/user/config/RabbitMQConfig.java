package com.parttime.user.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String AUDIT_EXCHANGE = "audit.direct";
    public static final String AUDIT_ROUTING_KEY = "enterprise.audit";
    public static final String AUDIT_QUEUE = "enterprise.audit.queue";

    @Bean
    public Exchange auditExchange() {
        return ExchangeBuilder.directExchange(AUDIT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .build();
    }

    @Bean
    public Binding auditBinding(Exchange auditExchange, Queue auditQueue) {
        return BindingBuilder.bind(auditQueue)
                .to(auditExchange)
                .with(AUDIT_ROUTING_KEY)
                .noargs();
    }
}