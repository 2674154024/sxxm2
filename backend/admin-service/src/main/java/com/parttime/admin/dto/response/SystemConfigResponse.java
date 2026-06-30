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
public class SystemConfigResponse {

    private Long id;

    private String configKey;

    private String configValue;

    private String configDesc;

    private Integer isEnabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}