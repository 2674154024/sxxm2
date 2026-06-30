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
@TableName("t_audit_log")
public class AuditLogEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("operator_id")
    private String operatorId;

    @TableField("module")
    private String module;

    @TableField("action")
    private String action;

    @TableField("request_params")
    private String requestParams;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("client_type")
    private String clientType;

    @TableField("trace_id")
    private String traceId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}