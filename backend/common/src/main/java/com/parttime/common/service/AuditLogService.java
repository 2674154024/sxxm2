package com.parttime.common.service;

import com.parttime.common.entity.AuditLogEntity;

/**
 * 审计日志服务接口
 */
public interface AuditLogService {

    /**
     * 异步保存审计日志
     *
     * @param auditLog 审计日志实体
     */
    void saveAsync(AuditLogEntity auditLog);
}