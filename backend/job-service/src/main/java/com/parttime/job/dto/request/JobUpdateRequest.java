package com.parttime.job.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class JobUpdateRequest {

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    private String jobTitle;

    private String jobType;

    private String industryTag;

    private Integer salaryType;

    @Positive(message = "薪资金额必须大于0")
    private BigDecimal salaryAmount;

    private Integer settlementType;

    private String workAddress;

    private String workTime;

    private String skillRequire;

    @Positive(message = "招聘人数必须大于0")
    private Integer recruitNum;

    private Integer isInsured;
}