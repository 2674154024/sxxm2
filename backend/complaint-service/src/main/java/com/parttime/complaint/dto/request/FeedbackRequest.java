package com.parttime.complaint.dto.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FeedbackRequest {

    @NotBlank(message = "被评价人ID不能为空")
    private String toId;

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;

    private String comment;
}