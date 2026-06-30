package com.parttime.notification.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.notification.dto.response.NotificationResponse;
import com.parttime.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    @AuditLog(module = "通知管理", action = "获取通知列表")
    public R<List<NotificationResponse>> getNotificationList(HttpServletRequest httpRequest,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "20") Integer size) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String userId = JwtUtil.getUserId(token);
        List<NotificationResponse> notifications = notificationService.getNotifications(userId, page, size);
        return R.ok(notifications);
    }

    @GetMapping("/unread-count")
    @AuditLog(module = "通知管理", action = "获取未读数量")
    public R<Map<String, Integer>> getUnreadCount(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String userId = JwtUtil.getUserId(token);
        Integer count = notificationService.getUnreadCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount", count);
        return R.ok(result);
    }

    @PutMapping("/read/{id}")
    @AuditLog(module = "通知管理", action = "标记已读")
    public R<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return R.ok();
    }

    @PutMapping("/read/all")
    @AuditLog(module = "通知管理", action = "全部标记已读")
    public R<Void> markAllAsRead(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String userId = JwtUtil.getUserId(token);
        notificationService.markAllAsRead(userId);
        return R.ok();
    }
}