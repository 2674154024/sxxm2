package com.parttime.task.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClockAppealRequest {

    @NotBlank(message = "记录ID不能为空")
    private String recordId;

    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;

    private String evidenceUrls;
}