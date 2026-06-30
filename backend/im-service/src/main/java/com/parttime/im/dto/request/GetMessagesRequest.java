package com.parttime.im.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GetMessagesRequest {

    @NotBlank(message = "目标用户ID不能为空")
    private String targetId;

    private Integer page = 1;

    private Integer size = 20;
}