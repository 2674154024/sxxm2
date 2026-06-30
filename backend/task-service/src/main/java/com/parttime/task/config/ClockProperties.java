package com.parttime.task.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "clock")
public class ClockProperties {

    private Integer maxDistance = 500;

    private Double workHourRound = 0.5;
}