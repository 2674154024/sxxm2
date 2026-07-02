package com.parttime.admin.config;

import com.parttime.admin.service.SystemConfigService;
import com.parttime.common.response.R;
import com.parttime.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/v1/pc/admin/login",
        "/v1/pc/admin/enterprise/register",
        "/v1/pc/admin/enterprise/login"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        if (PUBLIC_PATHS.contains(requestURI)) {
            return true;
        }

        String gatewayUserId = request.getHeader("X-User-Id");
        String gatewayRole = request.getHeader("X-Role");

        if (gatewayUserId != null && gatewayRole != null) {
            Integer role = null;
            try {
                role = Integer.parseInt(gatewayRole);
            } catch (NumberFormatException e) {
                log.warn("网关角色格式错误: {}", gatewayRole);
            }
            request.setAttribute("userId", gatewayUserId);
            request.setAttribute("X-User-Id", gatewayUserId);
            request.setAttribute("X-Role", gatewayRole);

            if (!validateRole(requestURI, role)) {
                responseJson(response, R.forbidden("无权限访问"));
                return false;
            }
            return true;
        }

        // if (!validateIpWhitelist(request)) {
        //     responseJson(response, R.forbidden("IP地址不在白名单中"));
        //     return false;
        // }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            responseJson(response, R.unauthorized("未授权"));
            return false;
        }

        token = token.substring(7);
        if (!JwtUtil.validateToken(token)) {
            responseJson(response, R.unauthorized("token无效或已过期"));
            return false;
        }

        Claims claims = JwtUtil.parseToken(token);
        String userId = claims.get("user_id", String.class);
        String roleStr = claims.get("role", String.class);
        Integer role = roleStr != null ? Integer.parseInt(roleStr) : null;

        request.setAttribute("userId", userId);
        request.setAttribute("X-User-Id", userId);
        request.setAttribute("X-Role", String.valueOf(role));

        if (!validateRole(requestURI, role)) {
            responseJson(response, R.forbidden("无权限访问"));
            return false;
        }

        return true;
    }

    private boolean validateIpWhitelist(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String whitelist = systemConfigService.getIpWhitelist();

        if (whitelist == null || whitelist.isEmpty()) {
            return true;
        }

        String[] ips = whitelist.split(",");
        for (String ip : ips) {
            ip = ip.trim();
            if (ip.equals(clientIp)) {
                return true;
            }
            if (ip.contains("/")) {
                if (isIpInRange(clientIp, ip)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isIpInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefix = Integer.parseInt(parts[1]);

            int[] ipParts = Arrays.stream(ip.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
            int[] networkParts = Arrays.stream(network.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();

            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (ipParts[i] != networkParts[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = 0xFF << (8 - remainingBits);
                if ((ipParts[fullBytes] & mask) != (networkParts[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateRole(String uri, Integer role) {
        if (role == 5) {
            return true;
        }

        if (uri.contains("/enterprise/audit") || uri.contains("/job/audit")) {
            return role == 1;
        }

        if (uri.contains("/risk/")) {
            return role == 2;
        }

        if (uri.contains("/operation/")) {
            return role == 3;
        }

        if (uri.contains("/finance/")) {
            return role == 4;
        }

        if (uri.contains("/system/")) {
            return role == 5;
        }

        return true;
    }

    private void responseJson(HttpServletResponse response, R<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(result.getCode() == 200 ? 200 : 403);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}