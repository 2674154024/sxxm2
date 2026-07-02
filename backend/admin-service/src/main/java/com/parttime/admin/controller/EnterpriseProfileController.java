package com.parttime.admin.controller;

import com.parttime.admin.dto.response.EnterpriseProfileResponse;
import com.parttime.admin.service.EnterpriseProfileService;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/enterprise")
public class EnterpriseProfileController {

    @Autowired
    private EnterpriseProfileService enterpriseProfileService;

    @GetMapping("/profile")
    public R<EnterpriseProfileResponse> getProfile(@RequestParam("enterpriseId") String enterpriseId) {
        EnterpriseProfileResponse response = enterpriseProfileService.getProfile(enterpriseId);
        return R.ok(response);
    }
}
