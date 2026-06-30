package com.parttime.admin.service;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.AdminLoginResponse;

public interface AdminService {

    AdminLoginResponse login(AdminLoginRequest request, String ip);
}