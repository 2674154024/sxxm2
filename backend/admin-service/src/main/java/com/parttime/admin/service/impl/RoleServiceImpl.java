package com.parttime.admin.service.impl;

import com.parttime.admin.dto.request.RoleRequest;
import com.parttime.admin.dto.response.RoleResponse;
import com.parttime.admin.entity.RoleEntity;
import com.parttime.admin.mapper.RoleMapper;
import com.parttime.admin.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleMapper.selectList(null).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(Integer id) {
        RoleEntity entity = roleMapper.selectById(id);
        return entity != null ? convertToResponse(entity) : null;
    }

    @Override
    @Transactional
    public void createRole(RoleRequest request) {
        RoleEntity entity = RoleEntity.builder()
            .id(Integer.parseInt(java.util.UUID.randomUUID().toString().substring(0, 8), 16) % 10000 + 1)
            .roleName(request.getRoleName())
            .roleCode(request.getRoleCode())
            .permissions(request.getPermissions())
            .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : 1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        roleMapper.insert(entity);
    }

    @Override
    @Transactional
    public void updateRole(Integer id, RoleRequest request) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new com.parttime.common.exception.BusinessException("角色不存在");
        }

        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setPermissions(request.getPermissions());
        if (request.getIsEnabled() != null) {
            entity.setIsEnabled(request.getIsEnabled());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new com.parttime.common.exception.BusinessException("角色不存在");
        }

        roleMapper.deleteById(id);
    }

    private RoleResponse convertToResponse(RoleEntity entity) {
        return RoleResponse.builder()
            .id(entity.getId())
            .roleName(entity.getRoleName())
            .roleCode(entity.getRoleCode())
            .permissions(entity.getPermissions())
            .isEnabled(entity.getIsEnabled())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}