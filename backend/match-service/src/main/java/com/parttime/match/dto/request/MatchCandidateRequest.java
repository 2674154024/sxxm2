package com.parttime.match.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchCandidateRequest {

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    private Integer limit = 20;
}