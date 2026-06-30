package com.parttime.common.entity;

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
@TableName("t_complaint")
public class ComplaintEntity {

    @TableId(value = "complaint_id", type = IdType.INPUT)
    private String complaintId;

    @TableField("student_id")
    private String studentId;

    @TableField("defendant_id")
    private String defendantId;

    @TableField("defendant_type")
    private String defendantType;

    @TableField("job_id")
    private String jobId;

    @TableField("complaint_type")
    private String complaintType;

    @TableField("complaint_content")
    private String complaintContent;

    @TableField("evidence_urls")
    private String evidenceUrls;

    @TableField("status")
    private Integer status;

    @TableField("handle_action")
    private String handleAction;

    @TableField("handle_amount")
    private BigDecimal handleAmount;

    @TableField("handle_result")
    private String handleResult;

    @TableField("handled_by")
    private String handledBy;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}