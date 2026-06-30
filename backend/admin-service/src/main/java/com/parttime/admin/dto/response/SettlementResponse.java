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
public class SettlementResponse {

    private String flowId;

    private String studentId;

    private String studentName;

    private String phone;

    private String jobId;

    private String jobTitle;

    private BigDecimal workHours;

    private BigDecimal hourlyRate;

    private BigDecimal grossAmount;

    private BigDecimal taxAmount;

    private BigDecimal netAmount;

    private Integer status;

    private LocalDateTime workDate;

    private LocalDateTime createdAt;
}