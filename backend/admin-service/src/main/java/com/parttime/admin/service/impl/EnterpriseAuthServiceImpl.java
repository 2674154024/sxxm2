package com.parttime.admin.service.impl;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.EnterpriseLoginResponse;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.service.EnterpriseAuthService;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EnterpriseAuthServiceImpl implements EnterpriseAuthService {

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Override
    public EnterpriseLoginResponse login(AdminLoginRequest request) {
        LambdaQueryWrapper<EnterpriseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEntity::getUsername, request.getUsername());
        EnterpriseEntity enterprise = enterpriseMapper.selectOne(wrapper);

        if (enterprise == null) {
            throw new BusinessException("用户名不存在");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!enterprise.getPassword().equals(encryptedPassword)) {
            throw new BusinessException("密码错误");
        }

        if (enterprise.getVerifyStatus() == 0) {
            throw new BusinessException("企业审核中，请耐心等待");
        } else if (enterprise.getVerifyStatus() == 1) {
            throw new BusinessException("企业审核未通过，请联系管理员");
        }

        String token = JwtUtil.generateToken(enterprise.getId(), "0", List.of(), 86400L);

        String phone = "";
        if (enterprise.getContactPhoneEncrypt() != null && !enterprise.getContactPhoneEncrypt().isEmpty()) {
            try {
                phone = AesUtil.decrypt(enterprise.getContactPhoneEncrypt());
            } catch (Exception e) {
                phone = enterprise.getContactPhoneEncrypt();
            }
        }

        return EnterpriseLoginResponse.builder()
                .token(token)
                .enterpriseId(enterprise.getId())
                .username(enterprise.getUsername())
                .enterpriseName(enterprise.getEnterpriseName())
                .creditCode(enterprise.getCreditCode())
                .legalPerson(enterprise.getLegalPerson())
                .phone(phone)
                .verifyStatus(enterprise.getVerifyStatus())
                .build();
    }
}