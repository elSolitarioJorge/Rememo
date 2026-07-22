package com.ggg.rememo.core.network;

import java.io.IOException;

/**
 * 可由 OkHttp/Retrofit 标准失败通道传递的结构化网络异常。
 */
public class ApiException extends IOException {
    private final NetworkErrorType type;
    private final int code;              // HTTP 状态码 或 业务 code
    private final String userMessage;    // 给用户展示的错误信息
    private final String debugMessage;   // 给开发调试使用的详细信息
    private final Throwable originalCause;
    private final boolean retryable;

    public ApiException(int code, String message) {
        this(resolveDefaultType(code), code, message, message, null, false);
    }

    public ApiException(NetworkErrorType type, int code, String userMessage, String debugMessage) {
        this(type, code, userMessage, debugMessage, null, false);
    }

    public ApiException(NetworkErrorType type, int code, String userMessage, String debugMessage, Throwable cause, boolean retryable) {
        super(userMessage, cause);
        this.type = type != null ? type : NetworkErrorType.UNKNOWN;
        this.code = code;
        this.userMessage = userMessage != null && !userMessage.isEmpty()
                ? userMessage
                : "网络异常，请稍后重试";
        this.debugMessage = debugMessage != null && !debugMessage.isEmpty()
                ? debugMessage
                : this.userMessage;
        this.originalCause = cause;
        this.retryable = retryable;
    }

    public NetworkErrorType getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public Throwable getOriginalCause() {
        return originalCause;
    }

    public boolean isRetryable() {
        return retryable;
    }

    @Override
    public String getMessage() {
        return userMessage;
    }

    private static NetworkErrorType resolveDefaultType(int code) {
        if (code == 401) {
            return NetworkErrorType.UNAUTHORIZED;
        }
        if (code == 403) {
            return NetworkErrorType.FORBIDDEN;
        }
        if (code == 404) {
            return NetworkErrorType.NOT_FOUND;
        }
        if (code == 500 || code == 502 || code == 503 || code == 504) {
            return NetworkErrorType.SERVER_ERROR;
        }
        if (code > 0) {
            return NetworkErrorType.BUSINESS_ERROR;
        }
        return NetworkErrorType.UNKNOWN;
    }
}
