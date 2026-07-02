package com.parttime.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_enterprise")
public class EnterpriseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("enterprise_name")
    private String enterpriseName;

    @TableField("credit_code")
    private String creditCode;

    @TableField("business_license_url")
    private String businessLicenseUrl;

    @TableField("legal_person")
    private String legalPerson;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone_encrypt")
    private String contactPhoneEncrypt;

    @TableField("verify_status")
    private Integer verifyStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("is_certified")
    private Integer isCertified;

    @TableField("credit_score")
    private Integer creditScore;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}