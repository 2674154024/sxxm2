package com.parttime.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String signName;

    private SmsTemplates templates = new SmsTemplates();

    @Data
    public static class SmsTemplates {
        private String interview;
        private String salary;
        private String audit;
    }
}