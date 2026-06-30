package com.parttime.task.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class AttendanceConfirmRequest {

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotBlank(message = "工作日期不能为空")
    private String workDate;

    @NotNull(message = "确认工时不能为空")
    private BigDecimal confirmedHours;

    private String note;
}