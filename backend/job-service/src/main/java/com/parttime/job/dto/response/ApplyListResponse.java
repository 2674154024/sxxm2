package com.parttime.job.dto.response;

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
public class ApplyListResponse {

    @JsonProperty("apply_id")
    private String applyId;

    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_name")
    private String jobTitle;

    private String jobType;

    private BigDecimal salaryAmount;

    private Integer settlementType;

    private String workAddress;

    private String enterpriseName;

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("student_phone")
    private String studentPhone;

    @JsonProperty("status")
    private String statusStr;

    @JsonProperty("apply_time")
    private String applyTime;

    private Integer applyStatus;

    private LocalDateTime interviewTime;

    private String interviewType;

    private LocalDateTime createdAt;
}