package com.parttime.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.JwtUtil;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.request.SmsCodeRequest;
import com.parttime.user.dto.request.WechatLoginRequest;
import com.parttime.user.dto.response.LoginResponse;
import com.parttime.user.entity.StudentEntity;
import com.parttime.user.mapper.StudentMapper;
import com.parttime.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    private static final long SMS_EXPIRE_SECONDS = 300;
    private static final long SMS_LIMIT_SECONDS = 60;

    @Override
    @AuditLog(module = "用户认证", action = "微信登录")
    @Transactional
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        String openid = mockWechatApiCall(request.getCode());

        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentEntity::getPhoneEncrypt, AesUtil.encrypt(openid));
        StudentEntity student = studentMapper.selectOne(wrapper);

        if (student == null) {
            student = StudentEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .verifyStatus(0)
                    .creditScore(100)
                    .phoneEncrypt(AesUtil.encrypt(openid))
                    .build();
            studentMapper.insert(student);
            log.info("首次微信登录，创建学生记录: {}", student.getId());
        }

        String token = JwtUtil.generateToken(student.getId(), "student", Arrays.asList("student:read", "student:write"));

        return LoginResponse.builder()
                .token(token)
                .userId(student.getId())
                .role("student")
                .realName(student.getRealName())
                .verifyStatus(student.getVerifyStatus())
                .creditScore(student.getCreditScore())
                .build();
    }

    @Override
    @AuditLog(module = "用户认证", action = "手机号登录")
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

        LambdaQueryWrapper<StudentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentEntity::getPhoneEncrypt, AesUtil.encrypt(request.getPhone()));
        StudentEntity student = studentMapper.selectOne(wrapper);

        if (student == null) {
            student = StudentEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .verifyStatus(0)
                    .creditScore(100)
                    .phoneEncrypt(AesUtil.encrypt(request.getPhone()))
                    .build();
            studentMapper.insert(student);
            log.info("首次手机号登录，创建学生记录: {}", student.getId());
        }

        redisTemplate.delete(redisKey);

        String token = JwtUtil.generateToken(student.getId(), "student", Arrays.asList("student:read", "student:write"));

        return LoginResponse.builder()
                .token(token)
                .userId(student.getId())
                .role("student")
                .realName(student.getRealName())
                .verifyStatus(student.getVerifyStatus())
                .creditScore(student.getCreditScore())
                .build();
    }

    @Override
    @AuditLog(module = "用户认证", action = "发送短信验证码")
    public void sendSmsCode(SmsCodeRequest request) {
        String limitKey = SMS_LIMIT_PREFIX + request.getPhone();
        String lastSendTime = redisTemplate.opsForValue().get(limitKey);

        if (lastSendTime != null) {
            throw new BusinessException("60秒内不允许重复发送");
        }

        String smsCode = generateSmsCode();

        String redisKey = SMS_CODE_PREFIX + request.getPhone();
        redisTemplate.opsForValue().set(redisKey, smsCode, SMS_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(limitKey, "1", SMS_LIMIT_SECONDS, TimeUnit.SECONDS);

        log.info("发送短信验证码: phone={}, code={}", request.getPhone(), smsCode);
    }

    private String mockWechatApiCall(String code) {
        return "mock_openid_" + code;
    }

    private String generateSmsCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}