package com.parttime.notification.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.notification.dto.response.NotificationResponse;
import com.parttime.notification.entity.NotificationEntity;
import com.parttime.notification.mapper.NotificationMapper;
import com.parttime.notification.service.NotificationService;
import com.parttime.notification.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private SmsUtil smsUtil;

    @Override
    @Transactional
    public void saveNotification(String userId, String userType, String title, String content, String notificationType, String relatedId) {
        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .userType(userType)
                .title(title)
                .content(content)
                .notificationType(notificationType)
                .isRead(0)
                .relatedId(relatedId)
                .createdAt(LocalDateTime.now())
                .build();

        notificationMapper.insert(notification);
    }

    @Override
    public List<NotificationResponse> getNotifications(String userId, Integer page, Integer size) {
        Page<NotificationEntity> pageParam = new Page<>(page, size);
        Page<NotificationEntity> result = notificationMapper.selectByUserId(pageParam, userId);
        return result.getRecords().stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadCount(String userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void sendSms(String phone, String templateCode, java.util.Map<String, String> params) {
        smsUtil.sendSms(phone, templateCode, params);
    }

    @Override
    public void handleSmsMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            String phone = json.getString("phone");
            String templateCode = json.getString("templateCode");
            JSONObject params = json.getJSONObject("params");

            java.util.Map<String, String> paramMap = new java.util.HashMap<>();
            if (params != null) {
                for (String key : params.keySet()) {
                    paramMap.put(key, params.getString(key));
                }
            }

            sendSms(phone, templateCode, paramMap);
        } catch (Exception e) {
        }
    }

    @Override
    public void handleImMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            String userId = json.getString("studentId");
            String type = json.getString("type");
            String title;
            String content;

            if ("complaint_handle".equals(type)) {
                title = "投诉处理通知";
                content = "您的投诉已处理，请查看处理结果";
            } else if ("interview_invite".equals(type)) {
                title = "面试邀请";
                content = "您收到了新的面试邀请";
            } else {
                title = "系统通知";
                content = message;
            }

            saveNotification(userId, "student", title, content, type, json.getString("complaintId"));
        } catch (Exception e) {
        }
    }

    @Override
    public void handleAuditMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            String userId = json.getString("userId");
            String type = json.getString("type");
            String title;
            String content;

            if ("enterprise_audit".equals(type)) {
                boolean approved = json.getBoolean("approved");
                title = "企业认证审核结果";
                content = approved ? "您的企业认证已通过" : "您的企业认证未通过";
            } else if ("job_audit".equals(type)) {
                boolean approved = json.getBoolean("approved");
                title = "岗位审核结果";
                content = approved ? "您发布的岗位已通过审核" : "您发布的岗位未通过审核";
            } else {
                title = "审核通知";
                content = message;
            }

            saveNotification(userId, json.getString("userType"), title, content, type, json.getString("relatedId"));
        } catch (Exception e) {
        }
    }

    private NotificationResponse convertToNotificationResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .notificationType(entity.getNotificationType())
                .isRead(entity.getIsRead())
                .relatedId(entity.getRelatedId())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }
}