package com.parttime.admin.service;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.response.EnterpriseLoginResponse;

public interface EnterpriseAuthService {

    EnterpriseLoginResponse login(AdminLoginRequest request);
}