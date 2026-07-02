package com.parttime.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CorsFilter implements GlobalFilter, Ordered {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        String path = request.getPath().value();
        String method = request.getMethodValue();
        
        log.info("CorsFilter处理请求: path={}, method={}, origin={}", path, method, origin);

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Disposition, X-User-Id, X-Role, X-Permissions");
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            log.info("CORS headers set for origin: {}", origin);
        }

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            log.info("OPTIONS请求，直接返回200");
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }
}