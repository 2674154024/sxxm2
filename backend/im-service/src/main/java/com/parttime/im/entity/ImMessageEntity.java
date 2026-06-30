package com.parttime.im.entity;

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
@TableName("t_im_message")
public class ImMessageEntity {

    @TableId(value = "message_id", type = IdType.INPUT)
    private String messageId;

    @TableField("from_id")
    private String fromId;

    @TableField("to_id")
    private String toId;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private String messageType;

    @TableField("timestamp")
    private Long timestamp;

    @TableField("is_read")
    private Integer isRead;

    @TableField("created_at")
    private LocalDateTime createdAt;
}