package com.parttime.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("t_attendance_record")
public class AttendanceRecordEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("work_date")
    private LocalDate workDate;

    @TableField("clock_in_time")
    private LocalDateTime clockInTime;

    @TableField("clock_out_time")
    private LocalDateTime clockOutTime;

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("confirmed_hours")
    private BigDecimal confirmedHours;

    @TableField("is_abnormal")
    private Integer isAbnormal;

    @TableField("settlement_status")
    private Integer settlementStatus;

    @TableField("note")
    private String note;

    @TableField("confirmed_at")
    private LocalDateTime confirmedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}