package com.parttime.job.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListResponse {

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_name")
    private String jobTitle;

    @JsonProperty("company_name")
    private String enterpriseName;

    @JsonProperty("is_certified")
    private Boolean isCertified;

    @JsonProperty("salary")
    private BigDecimal salaryAmount;

    @JsonProperty("salary_type")
    private String salaryTypeStr;

    @JsonProperty("settlement_type")
    private String settlementTypeStr;

    @JsonProperty("address")
    private String workAddress;

    @JsonProperty("distance")
    private Double distance;

    @JsonProperty("skill_tags")
    private List<String> skillTags;

    @JsonProperty("has_insurance")
    private Boolean hasInsurance;

    @JsonProperty("industry_tag")
    private String industryTag;

    @JsonProperty("status")
    private String statusStr;

    private String jobType;

    private Integer salaryType;

    private Integer settlementType;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer isInsured;

    private Integer viewCount;

    private Integer enterpriseCreditScore;

    private String createdAt;
}