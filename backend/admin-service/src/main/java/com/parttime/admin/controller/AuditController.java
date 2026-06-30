package com.parttime.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.EnterpriseAuditRequest;
import com.parttime.admin.dto.request.JobAuditRequest;
import com.parttime.admin.dto.response.EnterpriseAuditResponse;
import com.parttime.admin.dto.response.JobAuditResponse;
import com.parttime.admin.service.EnterpriseAuditService;
import com.parttime.admin.service.JobAuditService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/pc/admin")
public class AuditController {

    @Autowired
    private EnterpriseAuditService enterpriseAuditService;

    @Autowired
    private JobAuditService jobAuditService;

    @GetMapping("/enterprise/audit/list")
    public R<IPage<EnterpriseAuditResponse>> getEnterpriseAuditList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<EnterpriseAuditResponse> result = enterpriseAuditService.getEnterpriseAuditList(status, page, size);
        return R.ok(result);
    }

    @PostMapping("/enterprise/audit")
    @AuditLog(module = "企业审核", action = "审核")
    public R<Void> auditEnterprise(@Validated @RequestBody EnterpriseAuditRequest request,
                                   HttpServletRequest httpRequest) {
        String operatorId = httpRequest.getHeader("X-User-Id");
        enterpriseAuditService.auditEnterprise(request, operatorId);
        return R.ok();
    }

    @GetMapping("/job/audit/list")
    public R<IPage<JobAuditResponse>> getJobAuditList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<JobAuditResponse> result = jobAuditService.getJobAuditList(status, page, size);
        return R.ok(result);
    }

    @PostMapping("/job/audit")
    @AuditLog(module = "岗位审核", action = "审核")
    public R<Void> auditJob(@Validated @RequestBody JobAuditRequest request,
                            HttpServletRequest httpRequest) {
        String operatorId = httpRequest.getHeader("X-User-Id");
        jobAuditService.auditJob(request, operatorId);
        return R.ok();
    }
}