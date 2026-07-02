package com.parttime.admin.dto.response;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Integer id;

    private String roleName;

    private String roleCode;

    private String permissions;

    private Integer isEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public List<String> getPermissionList() {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(permissions, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}