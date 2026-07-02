package com.parttime.job.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobSearchRequest {

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal distance = new BigDecimal("5");

    private BigDecimal salaryMin = new BigDecimal("17");

    private BigDecimal salaryMax;

    private Integer settlementType;

    private String industryTag;

    private String keyword;

    private Integer isInsured;

    private String workTime;

    private Integer page = 1;

    private Integer size = 10;
}