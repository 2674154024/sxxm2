package com.parttime.admin.dto.response;

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

    private String id;

    private String jobTitle;

    private String enterpriseName;

    private String workAddress;

    private BigDecimal salaryAmount;

    private Integer salaryType;

    private Integer settlementType;

    private Integer status;

    private String skillRequire;

    private LocalDateTime createdAt;
}