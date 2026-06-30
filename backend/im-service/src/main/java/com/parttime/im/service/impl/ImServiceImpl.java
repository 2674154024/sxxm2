package com.parttime.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parttime.common.exception.BusinessException;
import com.parttime.im.config.WebSocketProperties;
import com.parttime.im.dto.response.ConversationResponse;
import com.parttime.im.dto.response.InviteInterviewResponse;
import com.parttime.im.dto.response.MessageResponse;
import com.parttime.im.entity.ImMessageEntity;
import com.parttime.im.entity.JobApplyEntity;
import com.parttime.im.mapper.ImMessageMapper;
import com.parttime.im.mapper.JobApplyMapper;
import com.parttime.im.service.ImService;
import com.parttime.im.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImServiceImpl implements ImService {

    private static final String UNREAD_MESSAGE_KEY = "im:unread:";

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private JobApplyMapper jobApplyMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WebSocketProperties webSocketProperties;

    @Override
    @Transactional
    public void sendMessage(String fromId, String targetId, String content, String messageType) {
        String messageId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ImMessageEntity message = ImMessageEntity.builder()
                .messageId(messageId)
                .fromId(fromId)
                .toId(targetId)
                .content(content)
                .messageType(messageType)
                .timestamp(timestamp)
                .isRead(0)
                .createdAt(LocalDateTime.now())
                .build();

        imMessageMapper.insert(message);

        if (WebSocketHandler.isOnline(targetId)) {
            JSONObject json = new JSONObject();
            json.put("type", "message");
            json.put("messageId", messageId);
            json.put("fromId", fromId);
            json.put("toId", targetId);
            json.put("content", content);
            json.put("messageType", messageType);
            json.put("timestamp", timestamp);
            WebSocketHandler.sendMessage(targetId, json.toJSONString());
            imMessageMapper.markMessagesAsRead(targetId, fromId);
        } else {
            List<String> unreadMessages = getUnreadMessagesFromRedis(targetId);
            if (unreadMessages.size() >= webSocketProperties.getMaxUnreadMessages()) {
                unreadMessages.remove(0);
            }
            unreadMessages.add(JSON.toJSONString(message));
            redisTemplate.opsForValue().set(UNREAD_MESSAGE_KEY + targetId, unreadMessages);
        }
    }

    @Override
    public List<MessageResponse> getMessageHistory(String userId, String targetId, Integer page, Integer size) {
        Page<ImMessageEntity> pageParam = new Page<>(page, size);
        Page<ImMessageEntity> result = imMessageMapper.selectMessagesBetweenPage(pageParam, userId, targetId);

        imMessageMapper.markMessagesAsRead(userId, targetId);

        return result.getRecords().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationResponse> getConversationList(String userId) {
        List<ImMessageMapper.ConversationListVO> voList = imMessageMapper.selectConversationList(userId);
        return voList.stream()
                .map(vo -> ConversationResponse.builder()
                        .targetId(vo.getTargetId())
                        .lastContent(vo.getLastContent())
                        .lastTimestamp(vo.getLastTimestamp())
                        .unreadCount(vo.getUnreadCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void pushUnreadMessages(String userId) {
        List<String> unreadMessages = getUnreadMessagesFromRedis(userId);
        if (!unreadMessages.isEmpty()) {
            for (String messageJson : unreadMessages) {
                WebSocketHandler.sendMessage(userId, "{\"type\":\"message\",\"data\":" + messageJson + "}");
            }
            redisTemplate.delete(UNREAD_MESSAGE_KEY + userId);
        }
    }

    @Override
    @Transactional
    public InviteInterviewResponse inviteInterview(String applyId, Long interviewTime) {
        JobApplyEntity apply = jobApplyMapper.selectByApplyId(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }

        String trtcRoomId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        jobApplyMapper.updateApplyStatus(applyId, 1, LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(interviewTime), java.time.ZoneId.systemDefault()), trtcRoomId);

        JSONObject notification = new JSONObject();
        notification.put("type", "interview_invite");
        notification.put("applyId", applyId);
        notification.put("trtcRoomId", trtcRoomId);
        notification.put("interviewTime", interviewTime);

        if (WebSocketHandler.isOnline(apply.getStudentId())) {
            WebSocketHandler.sendMessage(apply.getStudentId(), notification.toJSONString());
        } else {
            List<String> unreadMessages = getUnreadMessagesFromRedis(apply.getStudentId());
            if (unreadMessages.size() >= webSocketProperties.getMaxUnreadMessages()) {
                unreadMessages.remove(0);
            }
            unreadMessages.add(notification.toJSONString());
            redisTemplate.opsForValue().set(UNREAD_MESSAGE_KEY + apply.getStudentId(), unreadMessages);
        }

        return InviteInterviewResponse.builder()
                .applyId(applyId)
                .trtcRoomId(trtcRoomId)
                .interviewTime(interviewTime)
                .studentId(apply.getStudentId())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> getUnreadMessagesFromRedis(String userId) {
        Object obj = redisTemplate.opsForValue().get(UNREAD_MESSAGE_KEY + userId);
        if (obj instanceof List) {
            return (List<String>) obj;
        }
        return new ArrayList<>();
    }

    private MessageResponse convertToMessageResponse(ImMessageEntity entity) {
        return MessageResponse.builder()
                .messageId(entity.getMessageId())
                .fromId(entity.getFromId())
                .toId(entity.getToId())
                .content(entity.getContent())
                .messageType(entity.getMessageType())
                .timestamp(entity.getTimestamp())
                .isRead(entity.getIsRead())
                .build();
    }
}