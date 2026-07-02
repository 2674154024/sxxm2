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

    private String avatar;

    private String realName;

    private String gender;

    private String schoolName;

    private String phone;

    private String availableTime;

    private String skillTags;

    private String education;

    private String workExperience;

    private String selfIntroduction;

    private String major;

    private String grade;

    private Integer creditScore;
}