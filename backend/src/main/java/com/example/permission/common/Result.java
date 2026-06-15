package com.example.permission.common;

import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    
    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 500;
    private static final int UNAUTHORIZED_CODE = 401;
    private static final int FORBIDDEN_CODE = 403;
    
    public static <T> Result<T> success() {
        return success(null);
    }
    
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(UNAUTHORIZED_CODE);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.setCode(FORBIDDEN_CODE);
        result.setMessage(message);
        return result;
    }
}
