package com.parttime.im.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SendMessageRequest {

    @NotBlank(message = "目标用户ID不能为空")
    private String targetId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotNull(message = "消息类型不能为空")
    private String messageType;
}