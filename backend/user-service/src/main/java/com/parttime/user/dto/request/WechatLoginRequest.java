package com.parttime.user.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WechatLoginRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;
}