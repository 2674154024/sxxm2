package com.parttime.im.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private String conversationId;

    private String targetId;

    private String targetName;

    private String targetAvatar;

    private String lastContent;

    private Long lastTimestamp;

    private Integer unreadCount;

    private Boolean isOnline;
}