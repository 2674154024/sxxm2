package com.parttime.common.service.impl;

import com.parttime.common.entity.AuditLogEntity;
import com.parttime.common.mapper.AuditLogMapper;
import com.parttime.common.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * 审计日志服务实现类
 */
@Slf4j
@Service
@ConditionalOnBean(DataSource.class)
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Async("auditLogExecutor")
    public void saveAsync(AuditLogEntity auditLog) {
        try {
            auditLogMapper.insert(auditLog);
            log.debug("审计日志保存成功: traceId={}", auditLog.getTraceId());
        } catch (Exception e) {
            log.error("审计日志保存失败: {}", e.getMessage(), e);
        }
    }
}