package com.parttime.gateway.filter;

import com.parttime.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_PATH_PREFIX = "/v1/auth/";
    private static final String PUBLIC_PATH_PREFIX = "/v1/public/";

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("请求未携带Token: {}", path);
            return unauthorized(exchange);
        }

        String token = authorization.substring(7);
        try {
            if (!JwtUtil.validateToken(token)) {
                log.warn("Token无效或已过期: {}", path);
                return unauthorized(exchange);
            }

            String userId = JwtUtil.getUserId(token);
            String role = JwtUtil.getRole(token);
            String permissions = JwtUtil.getPermissions(token).toString();

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Role", role)
                    .header("X-Permissions", permissions)
                    .build();

            log.debug("Token验证通过: userId={}, role={}, path={}", userId, role, path);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return unauthorized(exchange);
        }
    }

    private boolean isWhiteList(String path) {
        return path.startsWith(AUTH_PATH_PREFIX) || path.startsWith(PUBLIC_PATH_PREFIX)
<<<<<<< HEAD
                || path.contains("/auth/register") || path.contains("/auth/login")
                || path.contains("/auth/sms-code") || path.contains("/auth/wechat")
                || path.equals("/v1/pc/admin/login");
=======
                || path.contains("/auth/register") || path.contains("/auth/login");
>>>>>>> 5b80af1a326ea41e292b4b1c528588055fc89dfc
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "未授权，请先登录");
        body.put("data", null);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("构建401响应失败: {}", e.getMessage());
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}