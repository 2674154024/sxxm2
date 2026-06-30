package com.parttime.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseProfileResponse {

    private String enterpriseId;

    private String enterpriseName;

    private String creditCode;

    private String businessLicenseUrl;

    private String legalPerson;

    private String contactName;

    private String contactPhone;

    private Integer verifyStatus;

    private Integer isCertified;

    private Integer creditScore;
}