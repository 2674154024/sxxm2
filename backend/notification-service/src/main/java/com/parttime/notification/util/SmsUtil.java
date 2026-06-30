package com.parttime.notification.util;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.parttime.notification.config.SmsCooldownProperties;
import com.parttime.notification.config.SmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class);

    private static final String SMS_COOLDOWN_KEY = "sms:cooldown:";

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private SmsCooldownProperties cooldownProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void sendSms(String phone, String templateCode, Map<String, String> params) {
        if (phone == null || phone.isEmpty()) {
            return;
        }

        String cooldownKey = SMS_COOLDOWN_KEY + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return;
        }

        try {
            Config config = new Config()
                    .setAccessKeyId(smsProperties.getAccessKeyId())
                    .setAccessKeySecret(smsProperties.getAccessKeySecret());
            config.endpoint = "dysmsapi.aliyuncs.com";

            Client client = new Client(config);

            String templateParam = JSON.toJSONString(params);

            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            SendSmsResponse response = client.sendSms(request);

            logger.info("短信发送结果: phone={}, code={}, message={}", phone,
                    response.body.code, response.body.message);

            redisTemplate.opsForValue().set(cooldownKey, "1", cooldownProperties.getCooldown(), TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            logger.error("短信发送失败: phone={}", phone, e);
        }
    }
}