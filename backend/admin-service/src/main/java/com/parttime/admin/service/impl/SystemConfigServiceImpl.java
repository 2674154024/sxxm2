package com.parttime.admin.service.impl;

import com.parttime.admin.dto.request.SystemConfigUpdateRequest;
import com.parttime.admin.dto.response.SystemConfigResponse;
import com.parttime.admin.entity.SystemConfigEntity;
import com.parttime.admin.mapper.SystemConfigMapper;
import com.parttime.admin.service.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String IP_WHITELIST_KEY = "admin.ip.whitelist";

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigMapper.selectList(null).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public SystemConfigResponse getConfigByKey(String configKey) {
        SystemConfigEntity entity = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, configKey)
        );
        return entity != null ? convertToResponse(entity) : null;
    }

    @Override
    @Transactional
    public void updateConfig(SystemConfigUpdateRequest request) {
        SystemConfigEntity entity = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, request.getConfigKey())
        );

        if (entity == null) {
            entity = SystemConfigEntity.builder()
                .configKey(request.getConfigKey())
                .isEnabled(1)
                .createdAt(LocalDateTime.now())
                .build();
            systemConfigMapper.insert(entity);
        } else {
            entity.setConfigValue(request.getConfigValue());
            entity.setConfigDesc(request.getConfigDesc());
            entity.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.updateById(entity);
        }
    }

    @Override
    public String getIpWhitelist() {
        SystemConfigEntity entity = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, IP_WHITELIST_KEY)
        );
        return entity != null ? entity.getConfigValue() : "127.0.0.1,192.168.0.0/16";
    }

    @Override
    @Transactional
    public void updateIpWhitelist(String ipWhitelist) {
        SystemConfigEntity entity = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfigEntity>()
                .eq(SystemConfigEntity::getConfigKey, IP_WHITELIST_KEY)
        );

        if (entity == null) {
            entity = SystemConfigEntity.builder()
                .configKey(IP_WHITELIST_KEY)
                .configDesc("管理后台IP白名单")
                .isEnabled(1)
                .createdAt(LocalDateTime.now())
                .build();
            systemConfigMapper.insert(entity);
        } else {
            entity.setConfigValue(ipWhitelist);
            entity.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.updateById(entity);
        }
    }

    private SystemConfigResponse convertToResponse(SystemConfigEntity entity) {
        return SystemConfigResponse.builder()
            .id(entity.getId())
            .configKey(entity.getConfigKey())
            .configValue(entity.getConfigValue())
            .configDesc(entity.getConfigDesc())
            .isEnabled(entity.getIsEnabled())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}