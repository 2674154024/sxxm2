package com.parttime.user.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class EnterpriseRegisterRequest {

    @NotBlank(message = "企业名称不能为空")
    private String enterpriseName;

    @NotBlank(message = "统一社会信用代码不能为空")
    private String creditCode;

    private String businessLicenseUrl;

    @NotBlank(message = "法人姓名不能为空")
    private String legalPerson;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "联系人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;
}