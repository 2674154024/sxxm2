package com.parttime.common.exception;

import com.parttime.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.badRequest(message);
    }

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return R.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("数据完整性约束冲突: {}", ex.getMessage());
        String message = "数据冲突，请检查输入";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("唯一")) {
                message = "该数据已存在";
            } else if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("外键")) {
                message = "关联数据不存在";
            }
        }
        return R.fail(409, message);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.warn("SQL约束冲突: {}", ex.getMessage());
        String message = "数据约束冲突";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().contains("唯一")) {
                message = "该数据已存在";
            }
        }
        return R.fail(409, message);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return R.serverError("系统繁忙，请稍后重试: " + ex.getClass().getSimpleName());
    }
}