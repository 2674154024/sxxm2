package com.parttime.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {

    private Integer heartbeatInterval = 30000;

    private Integer maxHeartbeatFailures = 3;

    private Integer maxUnreadMessages = 50;
}