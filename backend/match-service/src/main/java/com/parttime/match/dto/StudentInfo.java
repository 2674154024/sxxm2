package com.parttime.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfo {

    private String studentId;

    private String realName;

    private String avatarUrl;

    private String skillTags;

    private String availableTime;

    private Integer creditScore;

    private BigDecimal longitude;

    private BigDecimal latitude;
}