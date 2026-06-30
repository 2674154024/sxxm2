package com.parttime.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * <p>
 * 标注在需要记录审计日志的方法上，AOP切面会自动拦截并记录操作信息。
 * </p>
 * <p>
 * 记录内容：operator_id（从JWT获取）、module、action、request_params（脱敏）、ip_address、client_type、trace_id
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作模块（如：用户管理、岗位管理）
     */
    String module();

    /**
     * 操作类型（如：新增、修改、删除、查询）
     */
    String action();
}