package com.parttime.im.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String messageId;

    private String fromId;

    private String toId;

    private String content;

    private String messageType;

    private Long timestamp;

    private Integer isRead;
}