package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.admin.dto.request.JobAuditRequest;
import com.parttime.admin.dto.response.JobAuditResponse;
import com.parttime.admin.mapper.EnterpriseMapper;
import com.parttime.admin.mapper.JobMapper;
import com.parttime.admin.service.JobAuditService;
import com.parttime.common.entity.JobEntity;
import com.parttime.common.entity.EnterpriseEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobAuditServiceImpl implements JobAuditService {

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Override
    public IPage<JobAuditResponse> getJobAuditList(Integer status, Integer page, Integer size) {
        Page<JobEntity> pageParam = new Page<>(page, size);
        IPage<JobEntity> jobPage = jobMapper.selectPage(pageParam,
            new LambdaQueryWrapper<JobEntity>()
                .eq(status != null, JobEntity::getStatus, status)
                .orderByDesc(JobEntity::getCreatedAt)
        );

        return jobPage.convert(this::convertToResponse);
    }

    @Override
    @Transactional
    public void auditJob(JobAuditRequest request, String operatorId) {
        JobEntity job = jobMapper.selectById(request.getJobId());
        if (job == null) {
            throw new com.parttime.common.exception.BusinessException("岗位不存在");
        }

        job.setStatus(request.getStatus());
        job.setUpdatedAt(LocalDateTime.now());
        jobMapper.updateById(job);
    }

    private JobAuditResponse convertToResponse(JobEntity entity) {
        EnterpriseEntity enterprise = enterpriseMapper.selectById(entity.getEnterpriseId());
        String enterpriseName = enterprise != null ? enterprise.getEnterpriseName() : "";

        return JobAuditResponse.builder()
            .id(entity.getId())
            .jobTitle(entity.getJobTitle())
            .enterpriseName(enterpriseName)
            .workAddress(entity.getWorkAddress())
            .salaryAmount(entity.getSalaryAmount())
            .salaryType(entity.getSalaryType())
            .settlementType(entity.getSettlementType())
            .status(entity.getStatus())
            .skillRequire(entity.getSkillRequire())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}