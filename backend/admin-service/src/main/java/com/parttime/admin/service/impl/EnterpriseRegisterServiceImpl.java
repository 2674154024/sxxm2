package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parttime.admin.dto.request.EnterpriseRegisterRequest;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.service.EnterpriseRegisterService;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseRegisterServiceImpl implements EnterpriseRegisterService {

    private final EnterpriseMapper enterpriseMapper;

    @Override
    @Transactional
    public void register(EnterpriseRegisterRequest request) {
        LambdaQueryWrapper<EnterpriseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEntity::getCreditCode, request.getCreditCode());
        EnterpriseEntity existing = enterpriseMapper.selectOne(wrapper);

        if (existing != null) {
            throw new BusinessException(409, "该企业已注册");
        }

        wrapper.clear();
        wrapper.eq(EnterpriseEntity::getUsername, request.getUsername());
        EnterpriseEntity usernameExists = enterpriseMapper.selectOne(wrapper);

        if (usernameExists != null) {
            throw new BusinessException(409, "用户名已被使用");
        }

        String enterpriseId = UUID.randomUUID().toString();
        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());

        EnterpriseEntity enterprise = EnterpriseEntity.builder()
                .id(enterpriseId)
                .username(request.getUsername())
                .password(encryptedPassword)
                .enterpriseName(request.getEnterpriseName())
                .creditCode(request.getCreditCode())
                .legalPerson(request.getLegalPerson())
                .contactName(request.getContactName())
                .contactPhoneEncrypt(AesUtil.encrypt(request.getContactPhone()))
                .verifyStatus(1)
                .isCertified(0)
                .creditScore(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        enterpriseMapper.insert(enterprise);

        log.info("企业注册完成: enterpriseId={}, username={}, enterpriseName={}, verifyStatus=1(审核中)", 
                enterpriseId, request.getUsername(), request.getEnterpriseName());
    }
}