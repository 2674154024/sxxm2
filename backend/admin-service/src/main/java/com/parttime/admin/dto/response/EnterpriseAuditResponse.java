package com.parttime.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("enterprise_id")
    private String id;

    @JsonProperty("enterprise_name")
    private String enterpriseName;

    @JsonProperty("credit_code")
    private String creditCode;

    @JsonProperty("legal_person")
    private String legalPerson;

    @JsonProperty("contact_name")
    private String contactName;

    @JsonProperty("contact_phone")
    private String contactPhone;

    @JsonProperty("verify_status")
    private Integer verifyStatus;

    @JsonProperty("submit_time")
    private LocalDateTime createdAt;

    @JsonProperty("business_license")
    private String businessLicenseUrl;

    @JsonProperty("reject_reason")
    private String rejectReason;
}