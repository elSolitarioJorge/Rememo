package com.ggg.rememo.core.network;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.common.event.TokenExpiredEvent;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 统一处理 HTTP 错误和后端 {@code {code, message, data}} 业务错误。
 */
final class ApiResponseInterceptor implements Interceptor {

    interface TokenExpiredNotifier {
        void notifyExpired();
    }

    private static final Gson GSON = new Gson();

    private final TokenExpiredNotifier tokenExpiredNotifier;

    ApiResponseInterceptor() {
        this(() -> EventBus.getDefault().post(new TokenExpiredEvent()));
    }

    ApiResponseInterceptor(TokenExpiredNotifier tokenExpiredNotifier) {
        this.tokenExpiredNotifier = tokenExpiredNotifier;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (!response.isSuccessful()) {
            notifyTokenExpiredIfNeeded(response.code(), request);
            throw closeAndReturn(response,
                    NetworkErrorMapper.fromHttpCode(response.code(), request, null));
        }

        ApiResponse<?> envelope;
        try {
            envelope = inspectBoundedEnvelope(response);
        } catch (JsonParseException parseException) {
            throw closeAndReturn(response,
                    NetworkErrorMapper.fromThrowable(parseException, request));
        }

        if (envelope != null && !envelope.isSuccess()) {
            int businessCode = envelope.getCode();
            notifyTokenExpiredIfNeeded(businessCode, request);
            throw closeAndReturn(response, NetworkErrorMapper.fromBusinessCode(
                    businessCode,
                    envelope.getMessage(),
                    request
            ));
        }

        return response;
    }

    /**
     * peekBody 不消费真实响应体，但仍会复制数据。这里只复制有限字节。
     * 若 envelope 超过上限则跳过拦截器业务码检查，交给 Retrofit 和 Repository 正常解析，
     * 避免将截断后的合法大 JSON 误判成解析错误。
     */
    private ApiResponse<?> inspectBoundedEnvelope(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null || body.contentLength() == 0L) {
            return null;
        }

        long limit = NetworkGovernancePolicy.MAX_ENVELOPE_INSPECTION_BYTES;
        if (body.contentLength() > limit) {
            return null;
        }

        try (ResponseBody peekedBody = response.peekBody(limit + 1L)) {
            byte[] bytes = peekedBody.bytes();
            if (bytes.length > limit) {
                return null;
            }

            String bodyString = new String(bytes, StandardCharsets.UTF_8);
            if (bodyString.trim().isEmpty()) {
                return null;
            }
            return GSON.fromJson(bodyString, ApiResponse.class);
        }
    }

    private void notifyTokenExpiredIfNeeded(int code, Request request) {
        if (code == 401 && NetworkGovernancePolicy.isTokenProtectedRequest(request)) {
            tokenExpiredNotifier.notifyExpired();
        }
    }

    private ApiException closeAndReturn(Response response, ApiException exception) {
        response.close();
        return exception;
    }
}
