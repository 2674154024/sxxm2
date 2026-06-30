package com.parttime.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.JobAuditRequest;
import com.parttime.admin.dto.response.JobAuditResponse;

public interface JobAuditService {

    IPage<JobAuditResponse> getJobAuditList(Integer status, Integer page, Integer size);

    void auditJob(JobAuditRequest request, String operatorId);
}