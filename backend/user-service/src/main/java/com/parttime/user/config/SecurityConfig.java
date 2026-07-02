package com.parttime.user.config;

import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class SecurityConfig implements HandlerInterceptor {

    private static final Map<String, List<String>> URL_ROLE_MAP = new HashMap<>();

    static {
        URL_ROLE_MAP.put("/v1/student/auth/register", Arrays.asList("student"));
        URL_ROLE_MAP.put("/v1/student/profile", Arrays.asList("student"));
        URL_ROLE_MAP.put("/v1/student/resume", Arrays.asList("student"));
        URL_ROLE_MAP.put("/v1/student/job/apply", Arrays.asList("student"));
        URL_ROLE_MAP.put("/v1/student/job/favorite", Arrays.asList("student"));
        URL_ROLE_MAP.put("/v1/enterprise/auth/register", Arrays.asList("enterprise"));
        URL_ROLE_MAP.put("/v1/enterprise/profile", Arrays.asList("enterprise"));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(method)) {
            return true;
        }

        if (isPublicUrl(requestUri)) {
            return true;
        }

        String gatewayUserId = request.getHeader("X-User-Id");
        String gatewayRole = request.getHeader("X-Role");

        if (gatewayUserId != null && gatewayRole != null) {
            request.setAttribute("X-User-Id", gatewayUserId);
            request.setAttribute("X-Role", gatewayRole);
            checkPermission(requestUri, gatewayRole);
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未授权，请先登录");
        }

        token = token.substring(7);

        try {
            String role = JwtUtil.getRole(token);
            String userId = JwtUtil.getUserId(token);

            request.setAttribute("X-User-Id", userId);
            request.setAttribute("X-Role", role);

            checkPermission(requestUri, role);

            return true;
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }

    private boolean isPublicUrl(String uri) {
        return uri.startsWith("/v1/auth/") || uri.startsWith("/v1/public/")
                || uri.contains("/auth/register") || uri.contains("/auth/login")
                || uri.contains("/auth/sms-code") || uri.contains("/auth/wechat");
    }

    private void checkPermission(String uri, String role) {
        for (Map.Entry<String, List<String>> entry : URL_ROLE_MAP.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                if (!entry.getValue().contains(role)) {
                    throw new BusinessException(403, "无权限访问此资源");
                }
                return;
            }
        }
    }
}