package com.parttime.notification.consumer;

import com.parttime.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImConsumer {

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "parttime.queue.notification.im")
    public void handleIm(String message) {
        notificationService.handleImMessage(message);
    }
}