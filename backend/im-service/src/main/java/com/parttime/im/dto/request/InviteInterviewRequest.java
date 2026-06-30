package com.parttime.im.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InviteInterviewRequest {

    @NotBlank(message = "申请ID不能为空")
    private String applyId;

    @NotNull(message = "面试时间不能为空")
    private Long interviewTime;
}