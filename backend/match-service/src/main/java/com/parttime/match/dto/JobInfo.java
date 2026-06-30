package com.parttime.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo {

    private String jobId;

    private String jobTitle;

    private String jobType;

    private String industryTag;

    private Integer salaryType;

    private BigDecimal salaryAmount;

    private Integer settlementType;

    private String workAddress;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String workTime;

    private String skillRequire;

    private Integer status;

    private Integer isInsured;

    private Integer viewCount;

    private String enterpriseName;

    private Integer enterpriseCreditScore;
}