package com.parttime.complaint.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class ComplaintHandleRequest {

    @NotBlank(message = "投诉ID不能为空")
    private String complaintId;

    @NotBlank(message = "处理动作不能为空")
    private String action;

    private BigDecimal amount;

    @NotBlank(message = "处理结果不能为空")
    private String handleResult;

    private String handledBy;
}