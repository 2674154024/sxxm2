package com.parttime.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordResponse {

    private String id;

    private String studentId;

    private String jobId;

    private LocalDate workDate;

    private LocalDateTime clockInTime;

    private LocalDateTime clockOutTime;

    private BigDecimal workHours;

    private BigDecimal confirmedHours;

    private Integer isAbnormal;

    private Integer settlementStatus;

    private String note;
}