package com.parttime.job.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailResponse {

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

    private Integer recruitNum;

    private Integer status;

    private Integer isInsured;

    private Integer viewCount;

    private Integer applyCount;

    private String enterpriseName;

    private Integer enterpriseCreditScore;

    private Integer enterpriseVerifyStatus;

    private String contactPhone;

    private LocalDateTime createdAt;
}