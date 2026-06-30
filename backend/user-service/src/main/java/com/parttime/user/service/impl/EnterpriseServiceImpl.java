package com.parttime.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.JwtUtil;
import com.parttime.user.dto.request.EnterpriseRegisterRequest;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.response.EnterpriseProfileResponse;
import com.parttime.user.dto.response.LoginResponse;
import com.parttime.user.entity.EnterpriseEntity;
import com.parttime.user.mapper.EnterpriseMapper;
import com.parttime.user.service.EnterpriseService;
import com.parttime.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final AmqpTemplate amqpTemplate;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String AUDIT_EXCHANGE = "audit.direct";
    private static final String AUDIT_ROUTING_KEY = "enterprise.audit";
    private static final String SMS_CODE_PREFIX = "sms:code:";

    @Override
    @AuditLog(module = "企业认证", action = "企业注册")
    @Transactional
    public void register(EnterpriseRegisterRequest request) {
        LambdaQueryWrapper<EnterpriseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEntity::getCreditCode, request.getCreditCode());
        EnterpriseEntity existing = enterpriseMapper.selectOne(wrapper);

        if (existing != null) {
            throw new BusinessException(409, "该企业已注册");
        }

        String enterpriseId = UUID.randomUUID().toString();

        mockTianyanchaVerification(request.getCreditCode());

        EnterpriseEntity enterprise = EnterpriseEntity.builder()
                .id(enterpriseId)
                .enterpriseName(request.getEnterpriseName())
                .creditCode(request.getCreditCode())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .legalPerson(request.getLegalPerson())
                .contactName(request.getContactName())
                .contactPhoneEncrypt(AesUtil.encrypt(request.getContactPhone()))
                .verifyStatus(1)
                .isCertified(0)
                .creditScore(100)
                .build();

        enterpriseMapper.insert(enterprise);

        sendAuditMessage(enterpriseId);

        log.info("企业注册完成: enterpriseId={}, verifyStatus=1(审核中)", enterpriseId);
    }

    @Override
    @AuditLog(module = "企业管理", action = "查看企业信息")
    public EnterpriseProfileResponse getProfile(String enterpriseId) {
        EnterpriseEntity enterprise = enterpriseMapper.selectById(enterpriseId);

        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        return EnterpriseProfileResponse.builder()
                .enterpriseId(enterprise.getId())
                .enterpriseName(enterprise.getEnterpriseName())
                .creditCode(enterprise.getCreditCode())
                .businessLicenseUrl(enterprise.getBusinessLicenseUrl())
                .legalPerson(enterprise.getLegalPerson())
                .contactName(enterprise.getContactName())
                .contactPhone(DesensitizeUtil.desensitizePhone(AesUtil.decrypt(enterprise.getContactPhoneEncrypt())))
                .verifyStatus(enterprise.getVerifyStatus())
                .isCertified(enterprise.getIsCertified())
                .creditScore(enterprise.getCreditScore())
                .build();
    }

    @Override
    @AuditLog(module = "企业认证", action = "手机号登录")
    @Transactional
    public LoginResponse phoneLogin(PhoneLoginRequest request) {
        String redisKey = SMS_CODE_PREFIX + request.getPhone();
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!storedCode.equals(request.getSmsCode())) {
            throw new BusinessException("验证码错误");
        }

        LambdaQueryWrapper<EnterpriseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEntity::getContactPhoneEncrypt, AesUtil.encrypt(request.getPhone()));
        EnterpriseEntity enterprise = enterpriseMapper.selectOne(wrapper);

        if (enterprise == null) {
            throw new BusinessException("该手机号未注册企业账号");
        }

        redisTemplate.delete(redisKey);

        String token = JwtUtil.generateToken(enterprise.getId(), "enterprise", java.util.Arrays.asList("enterprise:read", "enterprise:write"));

        return LoginResponse.builder()
                .token(token)
                .userId(enterprise.getId())
                .role("enterprise")
                .realName(enterprise.getContactName())
                .verifyStatus(enterprise.getVerifyStatus())
                .creditScore(enterprise.getCreditScore())
                .build();
    }

    private void mockTianyanchaVerification(String creditCode) {
        log.info("调用天眼查API核验企业信用代码: {}", creditCode);
    }

    private void sendAuditMessage(String enterpriseId) {
        try {
            amqpTemplate.convertAndSend(AUDIT_EXCHANGE, AUDIT_ROUTING_KEY, enterpriseId);
            log.info("发送企业审核消息到MQ: enterpriseId={}", enterpriseId);
        } catch (Exception e) {
            log.error("发送审核消息失败: {}", e.getMessage());
        }
    }
}