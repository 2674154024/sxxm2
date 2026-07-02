package com.parttime.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseProfileResponse {

    private String id;

    private String enterpriseName;

    private String creditCode;

    private String businessLicenseUrl;

    private String legalPerson;

    private String contactName;

    private Integer verifyStatus;

    private Integer isCertified;

    private Integer creditScore;

    private LocalDateTime createdAt;
}
