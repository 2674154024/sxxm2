package com.parttime.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parttime.common.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志Mapper接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}