package com.parttime.match.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCandidateResponse {

    private String studentId;

    private String realName;

    private String avatarUrl;

    private String skillTags;

    private String availableTime;

    private Integer creditScore;

    private Integer matchScore;

    private Integer skillScore;

    private Integer timeScore;

    private Integer distanceScore;

    private Double distanceKm;
}