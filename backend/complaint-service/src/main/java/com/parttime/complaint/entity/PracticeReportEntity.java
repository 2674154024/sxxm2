package com.parttime.complaint.entity;

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
@TableName("t_practice_report")
public class PracticeReportEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("enterprise_comment")
    private String enterpriseComment;

    @TableField("student_comment")
    private String studentComment;

    @TableField("enterprise_score")
    private Integer enterpriseScore;

    @TableField("student_score")
    private Integer studentScore;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}