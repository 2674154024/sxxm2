package com.parttime.user.service;

import com.parttime.user.dto.request.EnterpriseRegisterRequest;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.response.EnterpriseProfileResponse;
import com.parttime.user.dto.response.LoginResponse;

public interface EnterpriseService {

    void register(EnterpriseRegisterRequest request);

    EnterpriseProfileResponse getProfile(String enterpriseId);

    LoginResponse phoneLogin(PhoneLoginRequest request);
}