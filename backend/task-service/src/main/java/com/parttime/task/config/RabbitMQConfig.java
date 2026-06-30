package com.parttime.task.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parttime.exchange";
    public static final String QUEUE_ATTENDANCE = "parttime.queue.attendance";
    public static final String ROUTING_KEY_ATTENDANCE = "attendance.#";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue attendanceQueue() {
        return new Queue(QUEUE_ATTENDANCE, true);
    }

    @Bean
    public Binding attendanceBinding(Queue attendanceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(attendanceQueue).to(exchange).with(ROUTING_KEY_ATTENDANCE);
    }
}