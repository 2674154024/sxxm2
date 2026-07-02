package com.parttime.user.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordLoginRequest {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
