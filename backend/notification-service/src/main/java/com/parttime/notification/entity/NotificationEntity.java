package com.parttime.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_notification")
public class NotificationEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("user_type")
    private String userType;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("notification_type")
    private String notificationType;

    @TableField("is_read")
    private Integer isRead;

    @TableField("related_id")
    private String relatedId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("read_at")
    private LocalDateTime readAt;
}