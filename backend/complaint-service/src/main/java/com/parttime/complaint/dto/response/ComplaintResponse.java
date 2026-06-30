package com.parttime.complaint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private String complaintId;

    private String studentId;

    private String defendantId;

    private String defendantType;

    private String jobId;

    private String complaintType;

    private String complaintContent;

    private String evidenceUrls;

    private Integer status;

    private String handleAction;

    private BigDecimal handleAmount;

    private String handleResult;

    private String handledBy;

    private LocalDateTime handledAt;

    private LocalDateTime createdAt;
}