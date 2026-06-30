package com.parttime.im.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.parttime.im.config.WebSocketProperties;
import com.parttime.im.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    public static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    public static final Map<String, AtomicInteger> HEARTBEAT_COUNTER_MAP = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeatExecutor = new ScheduledThreadPoolExecutor(2);

    @Autowired
    private WebSocketProperties webSocketProperties;

    @Autowired
    private ImService imService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.put(userId, session);
            HEARTBEAT_COUNTER_MAP.put(userId, new AtomicInteger(0));
            scheduleHeartbeat(userId);
            imService.pushUnreadMessages(userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject json = JSON.parseObject(payload);
        String type = json.getString("type");

        if ("pong".equals(type)) {
            String userId = (String) session.getAttributes().get("userId");
            if (userId != null) {
                HEARTBEAT_COUNTER_MAP.put(userId, new AtomicInteger(0));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.remove(userId);
            HEARTBEAT_COUNTER_MAP.remove(userId);
        }
    }

    private void scheduleHeartbeat(String userId) {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            AtomicInteger counter = HEARTBEAT_COUNTER_MAP.get(userId);
            if (counter != null) {
                int count = counter.incrementAndGet();
                if (count > webSocketProperties.getMaxHeartbeatFailures()) {
                    WebSocketSession session = SESSION_MAP.get(userId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.close();
                        } catch (IOException e) {
                        }
                    }
                    SESSION_MAP.remove(userId);
                    HEARTBEAT_COUNTER_MAP.remove(userId);
                } else {
                    WebSocketSession session = SESSION_MAP.get(userId);
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage("{\"type\":\"ping\"}"));
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }, webSocketProperties.getHeartbeatInterval(), webSocketProperties.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
    }

    public static void sendMessage(String userId, String message) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
            }
        }
    }

    public static boolean isOnline(String userId) {
        WebSocketSession session = SESSION_MAP.get(userId);
        return session != null && session.isOpen();
    }
}