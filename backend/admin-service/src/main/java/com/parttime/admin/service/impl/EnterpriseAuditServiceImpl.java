package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.admin.dto.request.EnterpriseAuditRequest;
import com.parttime.admin.dto.response.EnterpriseAuditResponse;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.service.EnterpriseAuditService;
import com.parttime.common.util.AesUtil;
import com.parttime.common.util.DesensitizeUtil;
import com.parttime.common.entity.EnterpriseEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EnterpriseAuditServiceImpl implements EnterpriseAuditService {

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    

    @Override
    public IPage<EnterpriseAuditResponse> getEnterpriseAuditList(Integer status, Integer page, Integer size) {
        Page<EnterpriseEntity> pageParam = new Page<>(page, size);
        IPage<EnterpriseEntity> enterprisePage = enterpriseMapper.selectPage(pageParam,
            new LambdaQueryWrapper<EnterpriseEntity>()
                .eq(status != null, EnterpriseEntity::getVerifyStatus, status)
                .orderByDesc(EnterpriseEntity::getCreatedAt)
        );

        return enterprisePage.convert(this::convertToResponse);
    }

    @Override
    @Transactional
    public void auditEnterprise(EnterpriseAuditRequest request, String operatorId) {
        EnterpriseEntity enterprise = enterpriseMapper.selectById(request.getEnterpriseId());
        if (enterprise == null) {
            throw new com.parttime.common.exception.BusinessException("企业不存在");
        }

        enterprise.setVerifyStatus(request.getStatus());
        enterprise.setUpdatedAt(LocalDateTime.now());
        enterpriseMapper.updateById(enterprise);
    }

    private EnterpriseAuditResponse convertToResponse(EnterpriseEntity entity) {
        String phone = "";
        if (entity.getContactPhoneEncrypt() != null && !entity.getContactPhoneEncrypt().isEmpty()) {
            try {
                phone = AesUtil.decrypt(entity.getContactPhoneEncrypt());
                phone = DesensitizeUtil.desensitizePhone(phone);
            } catch (Exception e) {
                phone = DesensitizeUtil.desensitizePhone(entity.getContactPhoneEncrypt());
            }
        }

        return EnterpriseAuditResponse.builder()
            .id(entity.getId())
            .enterpriseName(entity.getEnterpriseName())
            .creditCode(entity.getCreditCode())
            .legalPerson(entity.getLegalPerson())
            .contactName(entity.getContactName())
            .contactPhone(phone)
            .verifyStatus(entity.getVerifyStatus())
            .createdAt(entity.getCreatedAt())
            .businessLicenseUrl(entity.getBusinessLicenseUrl())
            .build();
    }
}