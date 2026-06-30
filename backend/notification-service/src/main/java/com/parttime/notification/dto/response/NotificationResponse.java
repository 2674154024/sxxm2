package com.parttime.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;

    private String title;

    private String content;

    private String notificationType;

    private Integer isRead;

    private String relatedId;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}