package com.parttime.job.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class JobAuditRequest {

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    private String auditReason;
}