package com.parttime.match.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRecommendResponse {

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

    private Integer status;

    private Integer isInsured;

    private Integer viewCount;

    private String enterpriseName;

    private Integer enterpriseCreditScore;

    private Integer matchScore;

    private Integer skillScore;

    private Integer timeScore;

    private Integer distanceScore;

    private Double distanceKm;
}