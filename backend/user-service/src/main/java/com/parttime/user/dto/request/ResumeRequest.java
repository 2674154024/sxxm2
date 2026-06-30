package com.parttime.user.dto.request;

import lombok.Data;

@Data
public class ResumeRequest {

    private String availableTime;

    private String skillTags;

    private String education;

    private String workExperience;

    private String selfIntroduction;
}