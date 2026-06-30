package com.parttime.gateway.filter;

import com.parttime.gateway.config.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitConfig rateLimitConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String ip = getClientIp(exchange);
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        RateLimitConfig.ApiLimit limit = getLimitConfig(path);
        String key = buildKey(path, ip, userId);

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofMillis(limit.getTimeWindow()));
            }

            if (count != null && count > limit.getMaxRequests()) {
                log.warn("请求限流: path={}, ip={}, userId={}, count={}", path, ip, userId, count);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            log.debug("请求计数: path={}, ip={}, userId={}, count={}", path, ip, userId, count);
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("限流处理异常: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private RateLimitConfig.ApiLimit getLimitConfig(String path) {
        if (path.startsWith("/v1/pc/admin")) {
            return rateLimitConfig.getAdminApi();
        } else if (path.startsWith("/v1/auth") || path.startsWith("/v1/public")) {
            return rateLimitConfig.getPublicApi();
        }
        return rateLimitConfig.getCoreApi();
    }

    private String buildKey(String path, String ip, String userId) {
        StringBuilder key = new StringBuilder("rate_limit:");
        if (userId != null && !userId.isEmpty()) {
            key.append("user:").append(userId);
        } else {
            key.append("ip:").append(ip);
        }
        key.append(":").append(path.replace("/", ":"));
        return key.toString();
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -90;
    }
}