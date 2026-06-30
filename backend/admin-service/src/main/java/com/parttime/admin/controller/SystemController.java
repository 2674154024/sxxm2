package com.parttime.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.parttime.admin.dto.request.RoleRequest;
import com.parttime.admin.dto.request.SystemConfigUpdateRequest;
import com.parttime.admin.dto.response.RoleResponse;
import com.parttime.admin.dto.response.SystemConfigResponse;
import com.parttime.admin.service.AuditService;
import com.parttime.admin.service.RoleService;
import com.parttime.admin.service.SystemConfigService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.entity.AuditLogEntity;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/pc/admin/system")
public class SystemController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AuditService auditService;

    @PutMapping("/config")
    @AuditLog(module = "系统配置", action = "修改")
    public R<Void> updateConfig(@Validated @RequestBody SystemConfigUpdateRequest request) {
        systemConfigService.updateConfig(request);
        return R.ok();
    }

    @GetMapping("/config")
    public R<List<SystemConfigResponse>> getAllConfigs() {
        List<SystemConfigResponse> result = systemConfigService.getAllConfigs();
        return R.ok(result);
    }

    @PostMapping("/role")
    @AuditLog(module = "角色管理", action = "新增")
    public R<Void> createRole(@Validated @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return R.ok();
    }

    @PutMapping("/role/{id}")
    @AuditLog(module = "角色管理", action = "修改")
    public R<Void> updateRole(@PathVariable Integer id, @Validated @RequestBody RoleRequest request) {
        roleService.updateRole(id, request);
        return R.ok();
    }

    @DeleteMapping("/role/{id}")
    @AuditLog(module = "角色管理", action = "删除")
    public R<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @GetMapping("/role")
    public R<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> result = roleService.getAllRoles();
        return R.ok(result);
    }

    @GetMapping("/audit-log/list")
    public R<IPage<AuditLogEntity>> getAuditLogList(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operatorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<AuditLogEntity> result = auditService.getAuditLogList(module, action, operatorId, page, size);
        return R.ok(result);
    }
}