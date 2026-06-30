package com.parttime.admin.service;

import com.parttime.admin.dto.request.SystemConfigUpdateRequest;
import com.parttime.admin.dto.response.SystemConfigResponse;

import java.util.List;

public interface SystemConfigService {

    List<SystemConfigResponse> getAllConfigs();

    SystemConfigResponse getConfigByKey(String configKey);

    void updateConfig(SystemConfigUpdateRequest request);

    String getIpWhitelist();

    void updateIpWhitelist(String ipWhitelist);
}