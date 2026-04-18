package com.ggg.rememo.core.network;

public class ApiException extends RuntimeException {
    private final int code;       // HTTP 状态码 或 业务 code
    private final String message; // 错误信息

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    @Override public String getMessage() { return message; }
}
