package com.parttime.common.entity;

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
@TableName("t_job_apply")
public class JobApplyEntity {

    @TableId(value = "apply_id", type = IdType.INPUT)
    private String applyId;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("apply_status")
    private Integer applyStatus;

    @TableField("interview_time")
    private LocalDateTime interviewTime;

    @TableField("interview_type")
    private String interviewType;

    @TableField("room_id")
    private String roomId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}