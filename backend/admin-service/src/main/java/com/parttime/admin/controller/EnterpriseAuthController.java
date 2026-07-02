package com.parttime.admin.controller;

import com.parttime.admin.dto.request.AdminLoginRequest;
import com.parttime.admin.dto.request.EnterpriseRegisterRequest;
import com.parttime.admin.dto.response.EnterpriseLoginResponse;
import com.parttime.admin.service.EnterpriseAuthService;
import com.parttime.admin.service.EnterpriseRegisterService;
import com.parttime.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pc/admin")
public class EnterpriseAuthController {

    @Autowired
    private EnterpriseRegisterService enterpriseRegisterService;

    @Autowired
    private EnterpriseAuthService enterpriseAuthService;

    @PostMapping("/enterprise/register")
    public R<String> register(@Validated @RequestBody EnterpriseRegisterRequest request) {
        enterpriseRegisterService.register(request);
        return R.ok("企业注册成功，等待审核");
    }

    @PostMapping("/enterprise/login")
    public R<EnterpriseLoginResponse> login(@Validated @RequestBody AdminLoginRequest request) {
        EnterpriseLoginResponse response = enterpriseAuthService.login(request);
        return R.ok(response);
    }
}