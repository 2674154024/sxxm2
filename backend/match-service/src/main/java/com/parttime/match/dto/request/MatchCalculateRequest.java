package com.parttime.match.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCalculateRequest {

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;
}