package com.parttime.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度在4到20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6到20个字符")
    private String password;

    @NotBlank(message = "企业名称不能为空")
    @Size(min = 2, max = 50, message = "企业名称长度在2到50个字符")
    private String enterpriseName;

    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[0-9A-Z]{18}$", message = "请输入有效的18位统一社会信用代码")
    private String creditCode;

    @NotBlank(message = "法定代表人不能为空")
    private String legalPerson;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String contactPhone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    private String email;

    private String address;
}