package com.example.permission.config;

import com.example.permission.common.BusinessException;
import com.example.permission.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理数据完整性异常（外键约束等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.warn("数据完整性异常: {}", e.getMessage());
        
        String message = "操作失败";
        Throwable cause = e.getCause();
        
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            String sqlMessage = cause.getMessage();
            if (sqlMessage != null) {
                if (sqlMessage.contains("foreign key") || sqlMessage.contains("FOREIGN KEY") 
                        || sqlMessage.contains("fk_") || sqlMessage.contains("FK_")) {
                    message = "该数据有关联数据，无法删除";
                } else if (sqlMessage.contains("Duplicate entry") || sqlMessage.contains("UNIQUE")) {
                    // 尝试提取重复的字段信息
                    if (sqlMessage.contains("username")) {
                        message = "用户名已存在";
                    } else if (sqlMessage.contains("role_key")) {
                        message = "角色标识已存在";
                    } else if (sqlMessage.contains("email")) {
                        message = "邮箱已被使用";
                    } else if (sqlMessage.contains("phone")) {
                        message = "手机号已被使用";
                    } else {
                        message = "数据已存在，请勿重复添加";
                    }
                } else if (sqlMessage.contains("cannot be null") || sqlMessage.contains("NOT NULL")) {
                    message = "必填字段不能为空";
                }
            }
        } else if (cause != null && cause.getMessage() != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage.contains("Duplicate")) {
                message = "数据已存在，请勿重复添加";
            } else if (causeMessage.contains("foreign key") || causeMessage.contains("constraint")) {
                message = "该数据有关联数据，无法删除";
            }
        }
        
        return Result.error(400, message);
    }
    
    /**
     * 处理主键重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        logger.warn("主键重复: {}", e.getMessage());
        return Result.error(400, "数据已存在，请勿重复添加");
    }
    
    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }
    
    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数绑定失败: {}", message);
        return Result.error(400, message);
    }
    
    /**
     * 处理参数校验异常（@RequestParam）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数校验失败: {}", message);
        return Result.error(400, message);
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        logger.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "认证失败，请重新登录");
    }
    
    /**
     * 处理凭证错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn("用户名或密码错误");
        return Result.error(401, "用户名或密码错误");
    }
    
    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足，无法访问该资源");
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("不支持的请求方法: {}", e.getMethod());
        return Result.error(405, "不支持的请求方法: " + e.getMethod());
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: ", e);
        String message = e.getMessage();
        if (message != null && message.length() < 100) {
            return Result.error(500, message);
        }
        return Result.error(500, "操作失败，请稍后重试");
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        logger.error("系统异常: ", e);
        return Result.error(500, "服务器错误，请稍后重试");
    }
}
