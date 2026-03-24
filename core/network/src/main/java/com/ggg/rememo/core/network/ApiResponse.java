package com.ggg.rememo.core.network;

/**
 * 统一 API 响应包装类。
 * 所有接口响应的 JSON 结构均为：{ code, message, data }
 *
 * @param <T> data 字段的实际数据类型
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return code == 200;
    }
}
