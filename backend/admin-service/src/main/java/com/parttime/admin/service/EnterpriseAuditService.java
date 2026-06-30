package com.parttime.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.EnterpriseAuditRequest;
import com.parttime.admin.dto.response.EnterpriseAuditResponse;

public interface EnterpriseAuditService {

    IPage<EnterpriseAuditResponse> getEnterpriseAuditList(Integer status, Integer page, Integer size);

    void auditEnterprise(EnterpriseAuditRequest request, String operatorId);
}