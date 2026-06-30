package com.parttime.notification.service;

import com.parttime.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void saveNotification(String userId, String userType, String title, String content, String notificationType, String relatedId);

    List<NotificationResponse> getNotifications(String userId, Integer page, Integer size);

    Integer getUnreadCount(String userId);

    void markAsRead(String id);

    void markAllAsRead(String userId);

    void sendSms(String phone, String templateCode, java.util.Map<String, String> params);

    void handleSmsMessage(String message);

    void handleImMessage(String message);

    void handleAuditMessage(String message);
}