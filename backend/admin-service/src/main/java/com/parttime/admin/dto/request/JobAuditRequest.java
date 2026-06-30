package com.parttime.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAuditRequest {

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotNull(message = "审核结果不能为空")
    private Integer status;

    private String remark;
}