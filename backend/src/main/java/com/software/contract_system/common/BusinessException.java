package com.software.contract_system.common;

/**
 * 业务异常类
 * 用于统一处理业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {
    
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    // 常用异常快捷方法
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
    
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
    
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
}

