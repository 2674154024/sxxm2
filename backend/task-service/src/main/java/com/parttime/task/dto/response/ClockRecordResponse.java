package com.parttime.task.dto.response;

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
public class ClockRecordResponse {

    private String id;

    private String jobId;

    private Integer clockType;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal distance;

    private Integer isAbnormal;

    private LocalDateTime clockTime;

    private BigDecimal workHours;

    private Integer settlementStatus;
}