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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_clock_record")
public class ClockRecordEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("clock_type")
    private Integer clockType;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("distance")
    private BigDecimal distance;

    @TableField("is_abnormal")
    private Integer isAbnormal;

    @TableField("clock_time")
    private LocalDateTime clockTime;

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("settlement_status")
    private Integer settlementStatus;

    @TableField("created_at")
    private LocalDateTime createdAt;
}