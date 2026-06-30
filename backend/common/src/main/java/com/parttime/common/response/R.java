package com.parttime.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 统一响应格式
 * <p>
 * 所有API接口返回此格式，包含状态码、消息、数据和追踪ID。
 * </p>
 * <p>
 * 状态码定义：
 * - 200: 成功
 * - 400: 请求参数错误
 * - 401: 未授权（登录过期或未登录）
 * - 403: 无权限
 * - 409: 冲突（重复提交等）
 * - 422: 业务校验失败
 * - 500: 系统异常
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求追踪ID（UUID）
     */
    @JsonProperty("trace_id")
    private String traceId;

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "成功", data, UUID.randomUUID().toString());
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return new R<>(200, "成功", null, UUID.randomUUID().toString());
    }

    /**
     * 成功响应（自定义消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, UUID.randomUUID().toString());
    }

    /**
     * 失败响应（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null, UUID.randomUUID().toString());
    }

    /**
     * 请求参数错误（400）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 400响应
     */
    public static <T> R<T> badRequest(String message) {
        return new R<>(400, message, null, UUID.randomUUID().toString());
    }

    /**
     * 未授权（401）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 401响应
     */
    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message, null, UUID.randomUUID().toString());
    }

    /**
     * 无权限（403）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 403响应
     */
    public static <T> R<T> forbidden(String message) {
        return new R<>(403, message, null, UUID.randomUUID().toString());
    }

    /**
     * 冲突（409）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 409响应
     */
    public static <T> R<T> conflict(String message) {
        return new R<>(409, message, null, UUID.randomUUID().toString());
    }

    /**
     * 业务校验失败（422）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 422响应
     */
    public static <T> R<T> unprocessable(String message) {
        return new R<>(422, message, null, UUID.randomUUID().toString());
    }

    /**
     * 系统异常（500）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 500响应
     */
    public static <T> R<T> serverError(String message) {
        return new R<>(500, message, null, UUID.randomUUID().toString());
    }

    /**
     * 判断是否成功
     *
     * @return true表示成功（code=200）
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}