package com.parttime.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintHandleRequest {

    @NotBlank(message = "投诉ID不能为空")
    private String complaintId;

    @NotBlank(message = "处理动作不能为空")
    private String action;

    private BigDecimal amount;

    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}