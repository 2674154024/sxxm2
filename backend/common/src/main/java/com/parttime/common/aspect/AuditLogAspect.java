package com.parttime.common.aspect;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.entity.AuditLogEntity;
import com.parttime.common.service.AuditLogService;
import com.parttime.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnBean(DataSource.class)
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(com.parttime.common.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String operatorId = "";
        String traceId = UUID.randomUUID().toString();

        if (request != null) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                try {
                    operatorId = JwtUtil.getUserId(token);
                } catch (Exception e) {
                    log.warn("解析JWT失败", e);
                }
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

        String module = auditLogAnnotation.module();
        String action = auditLogAnnotation.action();

        String requestParams = "";
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = signature.getParameterNames();
            StringBuilder paramsBuilder = new StringBuilder("{");
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                if (i > 0) {
                    paramsBuilder.append(", ");
                }
                String value = args[i] != null ? args[i].toString() : "null";
                paramsBuilder.append("\"").append(paramNames[i]).append("\": \"").append(value).append("\"");
            }
            paramsBuilder.append("}");
            requestParams = paramsBuilder.toString();
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
        }

        String ipAddress = request != null ? getClientIp(request) : "";
        String clientType = request != null ? request.getHeader("X-Client-Type") : "";

        AuditLogEntity auditLogEntity = AuditLogEntity.builder()
                .operatorId(operatorId)
                .module(module)
                .action(action)
                .requestParams(requestParams)
                .ipAddress(ipAddress)
                .clientType(clientType)
                .traceId(traceId)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogService.saveAsync(auditLogEntity);

        log.info("【审计日志】operatorId={}, module={}, action={}, requestParams={}, ipAddress={}, clientType={}, traceId={}",
                operatorId, module, action, requestParams, ipAddress, clientType, traceId);

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            log.error("【审计日志】操作失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}