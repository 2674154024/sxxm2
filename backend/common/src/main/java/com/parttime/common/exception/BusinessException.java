package com.parttime.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于封装业务逻辑中的校验失败、操作异常等情况，
 * 异常处理层会捕获此异常并返回对应的业务错误码。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final Integer code;

    /**
     * 创建业务异常（默认错误码422）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 422;
    }

    /**
     * 创建业务异常（自定义错误码）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}