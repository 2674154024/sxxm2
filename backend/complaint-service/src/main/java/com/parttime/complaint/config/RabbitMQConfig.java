package com.parttime.complaint.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parttime.exchange";
    public static final String QUEUE_COMPLAINT_ADMIN = "parttime.queue.complaint.admin";
    public static final String ROUTING_KEY_COMPLAINT_ADMIN = "complaint.admin";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue complaintAdminQueue() {
        return new Queue(QUEUE_COMPLAINT_ADMIN, true);
    }

    @Bean
    public Binding complaintAdminBinding(Queue complaintAdminQueue, TopicExchange exchange) {
        return BindingBuilder.bind(complaintAdminQueue).to(exchange).with(ROUTING_KEY_COMPLAINT_ADMIN);
    }
}