package com.example.srs.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码：200表示成功，500表示业务错误
    private String msg;   // 提示信息
    private T data;       // 实际返回的数据负载

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = success(data);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> unauthorized(String msg) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> forbidden(String msg) {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> conflict(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(409);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
