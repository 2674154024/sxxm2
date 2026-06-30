package com.parttime.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_clock_appeal")
public class ClockAppealEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("record_id")
    private String recordId;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("appeal_reason")
    private String appealReason;

    @TableField("evidence_urls")
    private String evidenceUrls;

    @TableField("appeal_status")
    private Integer appealStatus;

    @TableField("review_note")
    private String reviewNote;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}