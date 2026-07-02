package com.parttime.admin.service.impl;

import com.parttime.admin.dto.response.EnterpriseProfileResponse;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.service.EnterpriseProfileService;
import com.parttime.common.entity.EnterpriseEntity;
import com.parttime.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnterpriseProfileServiceImpl implements EnterpriseProfileService {

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Override
    public EnterpriseProfileResponse getProfile(String enterpriseId) {
        EnterpriseEntity enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        return EnterpriseProfileResponse.builder()
            .id(enterprise.getId())
            .enterpriseName(enterprise.getEnterpriseName())
            .creditCode(enterprise.getCreditCode())
            .businessLicenseUrl(enterprise.getBusinessLicenseUrl())
            .legalPerson(enterprise.getLegalPerson())
            .contactName(enterprise.getContactName())
            .verifyStatus(enterprise.getVerifyStatus())
            .isCertified(enterprise.getIsCertified())
            .creditScore(enterprise.getCreditScore())
            .createdAt(enterprise.getCreatedAt())
            .build();
    }
}
