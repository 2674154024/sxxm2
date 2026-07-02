package com.parttime.complaint.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ComplaintRequest {

    private String defendantId;

    private String defendantType;

    private String jobId;

    @NotBlank(message = "投诉类型不能为空")
    private String complaintType;

    @NotBlank(message = "投诉内容不能为空")
    private String complaintContent;

    private String evidenceUrls;
}