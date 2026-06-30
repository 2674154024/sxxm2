package com.parttime.complaint.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ComplaintRequest {

    @NotBlank(message = "被投诉人ID不能为空")
    private String defendantId;

    @NotBlank(message = "被投诉人类型不能为空")
    private String defendantType;

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotBlank(message = "投诉类型不能为空")
    private String complaintType;

    @NotBlank(message = "投诉内容不能为空")
    private String complaintContent;

    private String evidenceUrls;
}