package com.parttime.im.service;

import com.parttime.im.dto.response.ConversationResponse;
import com.parttime.im.dto.response.MessageResponse;
import com.parttime.im.dto.response.InviteInterviewResponse;

import java.util.List;

public interface ImService {

    void sendMessage(String fromId, String targetId, String content, String messageType);

    List<MessageResponse> getMessageHistory(String userId, String targetId, Integer page, Integer size);

    List<ConversationResponse> getConversationList(String userId);

    void pushUnreadMessages(String userId);

    InviteInterviewResponse inviteInterview(String applyId, Long interviewTime);
}