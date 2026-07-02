package com.parttime.im.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.im.dto.response.ConversationResponse;
import com.parttime.im.dto.response.MessageResponse;
import com.parttime.im.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/im")
public class ImController {

    @Autowired
    private ImService imService;

    @GetMapping("/conversations")
    @AuditLog(module = "即时通讯", action = "获取会话列表")
    public R<List<ConversationResponse>> getConversations(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
            return R.ok(new ArrayList<>());
        }
        try {
            String userId = JwtUtil.getUserId(token);
            List<ConversationResponse> conversations = imService.getConversationList(userId);
            return R.ok(conversations);
        } catch (Exception e) {
            return R.ok(new ArrayList<>());
        }
    }

    @GetMapping("/messages")
    @AuditLog(module = "即时通讯", action = "获取历史消息")
    public R<Map<String, Object>> getMessages(
            @RequestParam(required = false) String conversation_id,
            @RequestParam(required = false) String targetId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            return R.ok(result);
        }
        try {
            String userId = JwtUtil.getUserId(token);
            String targetIdParam = targetId;
            if (targetIdParam == null || targetIdParam.isEmpty()) {
                if (conversation_id != null && conversation_id.contains("_")) {
                    String[] parts = conversation_id.split("_");
                    targetIdParam = parts[0].equals(userId) ? parts[1] : parts[0];
                }
            }
            List<MessageResponse> messages = imService.getMessageHistory(userId, targetIdParam, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("list", messages);
            result.put("total", messages.size());
            return R.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0);
            return R.ok(result);
        }
    }
}
