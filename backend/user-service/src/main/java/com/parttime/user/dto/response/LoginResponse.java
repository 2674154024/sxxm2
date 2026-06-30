package com.parttime.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String userId;

    private String role;

    private String realName;

    private Integer verifyStatus;

    private Integer creditScore;
}