package com.ggg.rememo.core.network;

import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 网络层不依赖业务的固定策略。
 *
 * <p>把环境日志级别、鉴权请求判定和响应检查上限收敛在一起，便于纯 JVM 测试，
 * 避免拦截器与错误映射各自维护一份规则。</p>
 */
final class NetworkGovernancePolicy {

    static final String AUTH_PATH_PREFIX = "/api/auth/";

    /**
     * 只为检查统一响应 envelope 复制有限数据。超出上限时交给 Retrofit 正常解析，
     * 不能截断 JSON 后误判为解析失败。
     */
    static final long MAX_ENVELOPE_INSPECTION_BYTES = 1024L * 1024L;

    private NetworkGovernancePolicy() {
    }

    static HttpLoggingInterceptor.Level loggingLevel(boolean debug) {
        return debug
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE;
    }

    static boolean isAuthRequest(Request request) {
        return request != null
                && request.url().encodedPath().startsWith(AUTH_PATH_PREFIX);
    }

    static boolean hasAuthorization(Request request) {
        return request != null && request.header("Authorization") != null;
    }

    static boolean isTokenProtectedRequest(Request request) {
        return !isAuthRequest(request) && hasAuthorization(request);
    }
}
