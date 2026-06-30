package com.parttime.task.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClockRequest {

    @NotBlank(message = "岗位ID不能为空")
    private String jobId;

    @NotNull(message = "打卡类型不能为空")
    private Integer clockType;

    @NotNull(message = "经度不能为空")
    private Double longitude;

    @NotNull(message = "纬度不能为空")
    private Double latitude;
}