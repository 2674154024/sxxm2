package com.parttime.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {

    private String userId;

    private String realName;

    private String nickname;

    private String studentNo;

    private Long schoolId;

    private String phone;

    private String idCard;

    private String avatarUrl;

    private Integer verifyStatus;

    private Integer creditScore;

    private String availableTime;

    private String skillTags;
}