package com.parttime.common.exception;

import com.parttime.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用@RestControllerAdvice注解，统一处理Controller层抛出的异常，
 * 将异常转换为统一的响应格式R。
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     * <p>
     * 当@Valid注解校验失败时触发，返回400状态码和校验错误信息。
     * </p>
     *
     * @param ex MethodArgumentNotValidException
     * @return 400响应，包含所有校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(R.badRequest(message));
    }

    /**
     * 处理业务异常
     * <p>
     * 当业务逻辑校验失败时抛出，返回对应的业务错误码和消息。
     * </p>
     *
     * @param ex BusinessException
     * @return 业务错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(R.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理未知异常
     * <p>
     * 捕获所有未处理的异常，记录日志并返回500状态码，
     * 不向前端暴露具体堆栈信息。
     * </p>
     *
     * @param ex Exception
     * @return 500响应，友好的错误提示
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.serverError("系统繁忙，请稍后重试"));
    }
}