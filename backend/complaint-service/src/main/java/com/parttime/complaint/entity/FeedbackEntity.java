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
@TableName("t_feedback")
public class FeedbackEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("from_id")
    private String fromId;

    @TableField("from_type")
    private String fromType;

    @TableField("to_id")
    private String toId;

    @TableField("to_type")
    private String toType;

    @TableField("job_id")
    private String jobId;

    @TableField("practice_report_id")
    private String practiceReportId;

    @TableField("score")
    private Integer score;

    @TableField("comment")
    private String comment;

    @TableField("created_at")
    private LocalDateTime createdAt;
}