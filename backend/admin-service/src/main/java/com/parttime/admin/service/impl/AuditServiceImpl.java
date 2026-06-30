package com.parttime.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.common.mapper.AuditLogMapper;
import com.parttime.admin.service.AuditService;
import com.parttime.common.entity.AuditLogEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public IPage<AuditLogEntity> getAuditLogList(String module, String action, 
                                                  String operatorId, Integer page, Integer size) {
        Page<AuditLogEntity> pageParam = new Page<>(page, size);
        return auditLogMapper.selectPage(pageParam,
            new LambdaQueryWrapper<AuditLogEntity>()
                .eq(module != null && !module.isEmpty(), AuditLogEntity::getModule, module)
                .eq(action != null && !action.isEmpty(), AuditLogEntity::getAction, action)
                .eq(operatorId != null && !operatorId.isEmpty(), AuditLogEntity::getOperatorId, operatorId)
                .orderByDesc(AuditLogEntity::getCreatedAt)
        );
    }
}