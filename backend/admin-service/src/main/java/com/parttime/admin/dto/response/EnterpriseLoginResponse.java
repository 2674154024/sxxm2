package com.parttime.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseLoginResponse {

    private String token;

    private String enterpriseId;

    private String username;

    private String enterpriseName;

    private String creditCode;

    private String legalPerson;

    private String phone;

    private Integer verifyStatus;
}