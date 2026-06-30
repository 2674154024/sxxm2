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
public class EnterpriseAuditResponse {

    private String id;

    private String enterpriseName;

    private String creditCode;

    private String legalPerson;

    private String contactName;

    private String contactPhone;

    private Integer verifyStatus;

    private LocalDateTime createdAt;

    private String businessLicenseUrl;
}