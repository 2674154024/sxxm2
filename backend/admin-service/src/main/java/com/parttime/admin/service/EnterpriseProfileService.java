package com.parttime.admin.service;

import com.parttime.admin.dto.response.EnterpriseProfileResponse;

public interface EnterpriseProfileService {
    EnterpriseProfileResponse getProfile(String enterpriseId);
}
