package com.parttime.user.dto.request;

import lombok.Data;

@Data
public class StudentProfileUpdateRequest {

    private String realName;

    private String avatarUrl;

    private String availableTime;

    private String skillTags;
}