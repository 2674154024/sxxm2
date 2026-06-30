package com.parttime.user.controller;

import com.parttime.common.response.R;
import com.parttime.user.dto.request.EnterpriseRegisterRequest;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.response.EnterpriseProfileResponse;
import com.parttime.user.dto.response.LoginResponse;
import com.parttime.user.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @PostMapping("/auth/register")
    public R<String> register(@Validated @RequestBody EnterpriseRegisterRequest request) {
        enterpriseService.register(request);
        return R.ok("企业注册成功，等待审核");
    }

    @PostMapping("/auth/login")
    public R<LoginResponse> login(@Validated @RequestBody PhoneLoginRequest request) {
        return R.ok(enterpriseService.phoneLogin(request));
    }

    @GetMapping("/profile")
    public R<EnterpriseProfileResponse> getProfile(@RequestHeader("X-User-Id") String enterpriseId) {
        return R.ok(enterpriseService.getProfile(enterpriseId));
    }
}