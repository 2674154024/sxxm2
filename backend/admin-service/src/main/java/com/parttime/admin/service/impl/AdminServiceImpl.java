package com.parttime.admin.service.impl;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.AdminLoginResponse;
import com.parttime.admin.entity.AdminEntity;
import com.parttime.admin.entity.RoleEntity;
import com.parttime.admin.mapper.AdminMapper;
import com.parttime.admin.mapper.RoleMapper;
import com.parttime.admin.service.AdminService;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RoleMapper roleMapper;

    

    @Override
    public AdminLoginResponse login(AdminLoginRequest request, String ip) {
        AdminEntity admin = adminMapper.selectOne(
            new LambdaQueryWrapper<AdminEntity>()
                .eq(AdminEntity::getUsername, request.getUsername())
        );

        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (admin.getStatus() != 1) {
            throw new BusinessException(401, "账号已被禁用");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!encryptedPassword.equals(admin.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        RoleEntity role = roleMapper.selectById(admin.getRole());
        String roleName = role != null ? role.getRoleName() : "";

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", admin.getId());
        claims.put("role", admin.getRole());
        claims.put("permissions", role != null ? role.getPermissions() : "");

        List<String> permissionList = role != null && role.getPermissions() != null 
            ? Arrays.asList(role.getPermissions().split(",")) 
            : List.of();
        String token = JwtUtil.generateToken(admin.getId(), admin.getRole().toString(), 
            permissionList, 86400L);

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminMapper.updateById(admin);

        return AdminLoginResponse.builder()
            .token(token)
            .id(admin.getId())
            .username(admin.getUsername())
            .realName(admin.getRealName())
            .role(admin.getRole())
            .roleName(roleName)
            .build();
    }
}