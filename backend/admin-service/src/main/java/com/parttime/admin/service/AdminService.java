package com.parttime.admin.service;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.AdminLoginResponse;
import com.parttime.admin.dto.response.AdminProfileResponse;

public interface AdminService {

    AdminLoginResponse login(AdminLoginRequest request, String ip);

    AdminProfileResponse getProfile(String adminId);
}