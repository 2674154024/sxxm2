package com.parttime.admin.service;

import com.parttime.admin.dto.request.RoleRequest;
import com.parttime.admin.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Integer id);

    void createRole(RoleRequest request);

    void updateRole(Integer id, RoleRequest request);

    void deleteRole(Integer id);
}