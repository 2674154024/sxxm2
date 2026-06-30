package com.parttime.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    private ApiLimit publicApi = new ApiLimit();
    private ApiLimit coreApi = new ApiLimit();
    private ApiLimit adminApi = new ApiLimit();

    @Data
    public static class ApiLimit {
        private int maxRequests = 10;
        private long timeWindow = 60000;
    }
}