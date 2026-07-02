package com.parttime.user.service;

import com.parttime.user.dto.request.PasswordLoginRequest;
import com.parttime.user.dto.request.PhoneLoginRequest;
import com.parttime.user.dto.request.SmsCodeRequest;
import com.parttime.user.dto.request.StudentUserRegisterRequest;
import com.parttime.user.dto.request.WechatLoginRequest;
import com.parttime.user.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse wechatLogin(WechatLoginRequest request);

    LoginResponse phoneLogin(PhoneLoginRequest request);

    LoginResponse passwordLogin(PasswordLoginRequest request);

    LoginResponse register(StudentUserRegisterRequest request);

    void sendSmsCode(SmsCodeRequest request);
}