package com.software.contract_system.common;

import lombok.Data;

/**
 * 统一返回结果类
 * 所有接口都返回这个格式
 */
@Data
public class Result<T> {
    private Integer code; // 200表示成功，500表示失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    // 成功时调用
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    // 成功但无数据返回
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败时调用
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
    
    // 失败时调用（自定义错误码）
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}