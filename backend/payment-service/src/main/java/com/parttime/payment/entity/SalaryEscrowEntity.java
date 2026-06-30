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
@TableName("t_salary_escrow")
public class SalaryEscrowEntity {

    @TableId(value = "escrow_id", type = IdType.INPUT)
    private String escrowId;

    @TableField("enterprise_id")
    private String enterpriseId;

    @TableField("job_id")
    private String jobId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("paid_amount")
    private BigDecimal paidAmount;

    @TableField("freeze_amount")
    private BigDecimal freezeAmount;

    @TableField("available_amount")
    private BigDecimal availableAmount;

    @TableField("status")
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}