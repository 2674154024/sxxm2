package com.parttime.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private String userId;

    private String availableTime;

    private String skillTags;

    private String education;

    private String workExperience;

    private String selfIntroduction;

    private Integer creditScore;
}