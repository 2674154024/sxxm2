package com.parttime.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Integer id;

    private String roleName;

    private String roleCode;

    private String permissions;

    private Integer isEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}