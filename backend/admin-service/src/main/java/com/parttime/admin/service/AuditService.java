package com.parttime.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.common.entity.AuditLogEntity;

public interface AuditService {

    IPage<AuditLogEntity> getAuditLogList(String module, String action, String operatorId, 
                                          Integer page, Integer size);
}