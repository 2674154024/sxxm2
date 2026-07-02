package com.parttime.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class JobAuditResponse {

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("enterprise_id")
    private String enterpriseId;

    @JsonProperty("enterprise_name")
    private String enterpriseName;

    @JsonProperty("salary_amount")
    private BigDecimal salaryAmount;

    @JsonProperty("salary_type")
    private Integer salaryType;

    @JsonProperty("settlement_type")
    private Integer settlementType;

    @JsonProperty("work_address")
    private String workAddress;

    private Integer status;

    @JsonProperty("submit_time")
    private LocalDateTime submitTime;

    @JsonProperty("has_sensitive_word")
    private Boolean hasSensitiveWord;

    @JsonProperty("sensitive_words")
    private String[] sensitiveWords;
}