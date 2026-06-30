package com.parttime.user.controller;

import com.parttime.common.response.R;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.request.SmsCodeRequest;
import com.parttime.user.dto.request.WechatLoginRequest;
import com.parttime.user.dto.response.LoginResponse;
import com.parttime.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/wechat-login")
    public R<LoginResponse> wechatLogin(@Validated @RequestBody WechatLoginRequest request) {
        return R.ok(authService.wechatLogin(request));
    }

    @PostMapping("/phone-login")
    public R<LoginResponse> phoneLogin(@Validated @RequestBody PhoneLoginRequest request) {
        return R.ok(authService.phoneLogin(request));
    }

    @PostMapping("/sms-code")
    public R<String> sendSmsCode(@Validated @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request);
        return R.ok("验证码已发送");
    }
}