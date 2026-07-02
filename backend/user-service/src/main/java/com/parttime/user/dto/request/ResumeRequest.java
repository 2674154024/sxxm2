package com.parttime.user.dto.request;

import lombok.Data;

@Data
public class ResumeRequest {

    private String avatar;

    private String gender;

    private String realName;

    private String phone;

    private String availableTime;

    private String skillTags;

    private String education;

    private String workExperience;

    private String selfIntroduction;

    private String major;

    private String grade;
}