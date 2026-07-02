package com.parttime.im.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.parttime.im.dto.request.GetMessagesRequest;
import com.parttime.im.dto.request.SendMessageRequest;
import com.parttime.im.dto.response.ConversationResponse;
import com.parttime.im.dto.response.MessageResponse;
import com.parttime.im.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/student/im")
public class StudentImController {

    @Autowired
    private ImService imService;

    @PostMapping("/send")
    @AuditLog(module = "即时通讯", action = "发送消息")
    public R<Void> sendMessage(@Valid @RequestBody SendMessageRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
            return R.unauthorized("未授权，请先登录");
        }
        String userId = JwtUtil.getUserId(token);
        imService.sendMessage(userId, request.getTargetId(), request.getContent(), request.getMessageType());
        return R.ok();
    }

    @GetMapping("/messages")
    @AuditLog(module = "即时通讯", action = "获取历史消息")
    public R<List<MessageResponse>> getMessages(@Valid GetMessagesRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
            return R.ok(new ArrayList<>());
        }
        String userId = JwtUtil.getUserId(token);
        List<MessageResponse> messages = imService.getMessageHistory(userId, request.getTargetId(),
                request.getPage(), request.getSize());
        return R.ok(messages);
    }

    @GetMapping("/conversation-list")
    @AuditLog(module = "即时通讯", action = "获取会话列表")
    public R<List<ConversationResponse>> getConversationList(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty() || !JwtUtil.validateToken(token)) {
            return R.ok(new ArrayList<>());
        }
        String userId = JwtUtil.getUserId(token);
        List<ConversationResponse> conversations = imService.getConversationList(userId);
        return R.ok(conversations);
    }
}