package com.parttime.payment.entity;

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
@TableName("t_salary_flow")
public class SalaryFlowEntity {

    @TableId(value = "flow_id", type = IdType.INPUT)
    private String flowId;

    @TableField("student_id")
    private String studentId;

    @TableField("job_id")
    private String jobId;

    @TableField("enterprise_id")
    private String enterpriseId;

    @TableField("work_date")
    private LocalDateTime workDate;

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("hourly_wage")
    private BigDecimal hourlyRate;

    @TableField("gross_amount")
    private BigDecimal grossAmount;

    @TableField("tax_amount")
    private BigDecimal taxAmount;

    @TableField("net_amount")
    private BigDecimal netAmount;

    @TableField("settlement_status")
    private Integer status;

    @TableField("agreement_id")
    private String agreementId;

    @TableField("invoice_id")
    private String invoiceId;

    @TableField("trace_id")
    private String traceId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}