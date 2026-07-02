package com.parttime.admin.controller;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.AdminLoginResponse;
import com.parttime.admin.dto.response.AdminProfileResponse;
import com.parttime.admin.service.AdminService;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/pc/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    @AuditLog(module = "管理员登录", action = "登录")
    public R<AdminLoginResponse> login(@Validated @RequestBody AdminLoginRequest request, 
                                       HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        AdminLoginResponse response = adminService.login(request, ip);
        return R.ok(response);
    }

    @GetMapping("/profile")
    public R<AdminProfileResponse> getProfile(@RequestParam("adminId") String adminId) {
        AdminProfileResponse response = adminService.getProfile(adminId);
        return R.ok(response);
    }
}