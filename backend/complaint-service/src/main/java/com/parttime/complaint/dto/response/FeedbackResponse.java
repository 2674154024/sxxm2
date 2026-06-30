package com.parttime.complaint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private String id;

    private String fromId;

    private String fromType;

    private String toId;

    private String toType;

    private String jobId;

    private Integer score;

    private String comment;

    private LocalDateTime createdAt;
}