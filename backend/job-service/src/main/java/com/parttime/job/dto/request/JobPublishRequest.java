package com.parttime.job.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class JobPublishRequest {

    @NotBlank(message = "岗位标题不能为空")
    private String jobTitle;

    private String jobType;

    private String industryTag;

    @NotNull(message = "薪资类型不能为空")
    private Integer salaryType;

    @NotNull(message = "薪资金额不能为空")
    @Positive(message = "薪资金额必须大于0")
    private BigDecimal salaryAmount;

    @NotNull(message = "结算类型不能为空")
    private Integer settlementType;

    @NotBlank(message = "工作地址不能为空")
    private String workAddress;

    private String workTime;

    private String skillRequire;

    @NotNull(message = "招聘人数不能为空")
    @Positive(message = "招聘人数必须大于0")
    private Integer recruitNum;

    private Integer isInsured = 0;
}