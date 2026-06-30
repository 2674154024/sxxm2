package com.parttime.match.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreResponse {

    private String studentId;

    private String jobId;

    private Integer matchScore;

    private Integer skillScore;

    private Integer timeScore;

    private Integer distanceScore;

    private Double distanceKm;
}