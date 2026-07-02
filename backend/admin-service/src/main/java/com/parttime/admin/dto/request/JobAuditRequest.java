package com.parttime.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAuditRequest {

    @NotBlank(message = "岗位ID不能为空")
    @JsonProperty("job_id")
    private String jobId;

    private String action;

    private String reason;

    public Integer getStatus() {
        if ("pass".equals(action)) {
            return 1;
        } else if ("reject".equals(action)) {
            return 2;
        }
        return null;
    }
}