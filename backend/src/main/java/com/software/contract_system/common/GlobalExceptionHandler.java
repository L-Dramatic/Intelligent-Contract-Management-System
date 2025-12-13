package com.software.contract_system.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

/**
 * 全局异常处理器
 * 只要代码里抛出异常，都会被这里捕获，变成友好的 JSON 返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 捕获所有运行时异常 (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("系统运行时异常: ", e);
        // 直接把错误信息 (如 "密码错误", "余额不足") 返回给前端
        return Result.error(e.getMessage());
    }

    // 捕获其他未预料的异常 (Exception)
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统未知错误: ", e);
        return Result.error("系统繁忙，请联系管理员");
    }

    /**
     * ★★★ 新增：捕获权限不足异常 ★★★
     * 当 @PreAuthorize 验证失败时，抛出此异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限验证失败: ", e);
        // 手动设置状态码 403
        Result<String> result = new Result<>();
        result.setCode(403);
        result.setMsg("权限不足，拒绝访问");
        result.setData(null);
        return result;
    }
}