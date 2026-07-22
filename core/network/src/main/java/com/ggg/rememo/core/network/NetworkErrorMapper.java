package com.ggg.rememo.core.network;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Request;

/**
 * 将 HTTP 状态码、业务 code 和底层异常映射为统一的 ApiException。
 */
public final class NetworkErrorMapper {


    private NetworkErrorMapper() {
    }

    public static ApiException fromHttpCode(int httpCode, Request request, String serverMessage) {
        NetworkErrorType type;
        String userMessage;
        boolean retryable = false;

        switch (httpCode) {
            case 401:
                if (isTokenProtectedRequest(request)) {
                    type = NetworkErrorType.TOKEN_EXPIRED;
                    userMessage = "登录已过期";
                } else {
                    type = NetworkErrorType.UNAUTHORIZED;
                    userMessage = "请求未授权";
                }
                break;
            case 403:
                type = NetworkErrorType.FORBIDDEN;
                userMessage = "暂无权限访问该内容";
                break;
            case 404:
                type = NetworkErrorType.NOT_FOUND;
                userMessage = "内容不存在或已被删除";
                break;
            case 500:
            case 502:
            case 503:
            case 504:
                type = NetworkErrorType.SERVER_ERROR;
                userMessage = "服务器异常，请稍后重试";
                retryable = true;
                break;
            default:
                type = NetworkErrorType.UNKNOWN;
                userMessage = hasText(serverMessage) ? serverMessage : "请求失败，请稍后重试";
                break;
        }

        return new ApiException(
                type,
                httpCode,
                userMessage,
                buildDebugMessage("HTTP", httpCode, serverMessage, request),
                null,
                retryable
        );
    }

    public static ApiException fromBusinessCode(int bizCode, String bizMsg, Request request) {
        if (bizCode == 401) {
            NetworkErrorType type = isTokenProtectedRequest(request)
                    ? NetworkErrorType.TOKEN_EXPIRED
                    : NetworkErrorType.UNAUTHORIZED;
            String userMessage = hasText(bizMsg)
                    ? bizMsg
                    : (type == NetworkErrorType.TOKEN_EXPIRED ? "登录已过期" : "认证失败");
            return new ApiException(
                    type,
                    bizCode,
                    userMessage,
                    buildDebugMessage("BUSINESS", bizCode, bizMsg, request),
                    null,
                    false
            );
        }

        String userMessage = hasText(bizMsg) ? bizMsg : "操作失败，请稍后重试";
        return new ApiException(
                NetworkErrorType.BUSINESS_ERROR,
                bizCode,
                userMessage,
                buildDebugMessage("BUSINESS", bizCode, bizMsg, request),
                null,
                false
        );
    }

    public static ApiException fromThrowable(Throwable t) {
        return fromThrowable(t, null);
    }

    public static String toUserMessage(Throwable t) {
        return fromThrowable(t).getUserMessage();
    }

    static ApiException fromThrowable(Throwable t, Request request) {
        if (t instanceof ApiException) {
            return (ApiException) t;
        }

        NetworkErrorType type;
        String userMessage;
        boolean retryable;

        if (t instanceof UnknownHostException) {
            type = NetworkErrorType.DNS_ERROR;
            userMessage = "无法连接服务器，请检查网络或地址配置";
            retryable = true;
        } else if (t instanceof ConnectException) {
            type = NetworkErrorType.CONNECT_ERROR;
            userMessage = "服务器连接失败，请稍后重试";
            retryable = true;
        } else if (t instanceof SocketTimeoutException) {
            type = NetworkErrorType.TIMEOUT;
            userMessage = "请求超时，请稍后重试";
            retryable = true;
        } else if (t instanceof JsonSyntaxException
                || t instanceof JsonParseException
                || t instanceof MalformedJsonException) {
            type = NetworkErrorType.PARSE_ERROR;
            userMessage = "数据解析失败，请稍后重试";
            retryable = false;
        } else {
            type = NetworkErrorType.UNKNOWN;
            userMessage = "网络异常，请稍后重试";
            retryable = true;
        }

        return new ApiException(
                type,
                0,
                userMessage,
                buildDebugMessage(t, request),
                t,
                retryable
        );
    }

    private static boolean isTokenProtectedRequest(Request request) {
        return NetworkGovernancePolicy.isTokenProtectedRequest(request);
    }

    private static String buildDebugMessage(String source, int code, String message, Request request) {
        return source
                + " code=" + code
                + ", message=" + (message != null ? message : "")
                + ", path=" + requestPath(request);
    }

    private static String buildDebugMessage(Throwable t, Request request) {
        if (t == null) {
            return "UNKNOWN, path=" + requestPath(request);
        }
        return t.getClass().getName()
                + ": " + (t.getMessage() != null ? t.getMessage() : "")
                + ", path=" + requestPath(request);
    }

    private static String requestPath(Request request) {
        return request == null ? "" : request.url().encodedPath();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
