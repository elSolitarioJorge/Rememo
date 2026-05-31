package com.ggg.rememo.core.network;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.common.event.TokenExpiredEvent;
import com.ggg.rememo.core.common.util.TokenManager;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络客户端单例。
 * 负责初始化 OkHttpClient 和 Retrofit 实例。
 */
public class NetworkClient {

    private static final String AUTH_PATH_PREFIX = "/api/auth/";
    private static final String BASE_URL = BuildConfig.BASE_URL;

    private static volatile NetworkClient instance;

    private final ApiService apiService;

    private NetworkClient() {
        OkHttpClient okHttpClient = buildOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static NetworkClient getInstance() {
        if (instance == null) {
            synchronized (NetworkClient.class) {
                if (instance == null) {
                    instance = new NetworkClient();
                }
            }
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    private static boolean isAuthRequest(Request request) {
        return request != null && request.url().encodedPath().startsWith(AUTH_PATH_PREFIX);
    }

    private static boolean hasAuthorization(Request request) {
        return request != null && request.header("Authorization") != null;
    }

    private static boolean isTokenProtectedRequest(Request request) {
        return !isAuthRequest(request) && hasAuthorization(request);
    }

    private static void postTokenExpiredIfNeeded(Request request) {
        if (isTokenProtectedRequest(request)) {
            EventBus.getDefault().post(new TokenExpiredEvent());
        }
    }

    private OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(new ResponseInterceptor())
                .addInterceptor(loggingInterceptor)
                .build();
    }

    /**
     * 自动注入 Authorization Header 的拦截器。
     * 直接从 TokenManager（MMKV 持久化存储）读取 Token，
     * 保证 App 重启后仍能自动恢复登录态。
     */
    private static class AuthInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            if (isAuthRequest(original)) {
                return chain.proceed(original);
            }

            String token = TokenManager.getToken();
            if (token != null && !token.isEmpty()) {
                Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + token);
                return chain.proceed(builder.build());
            }

            return chain.proceed(original);
        }
    }

    /**
     * 处理 HTTP 响应的拦截器。
     */
    private static class ResponseInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            // HTTP 状态码非 2xx，统一处理
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    postTokenExpiredIfNeeded(request);
                }
                throw NetworkErrorMapper.fromHttpCode(response.code(), request, null);
            }

            // HTTP 2xx 情况下，检查业务 code
            // 注意：peekBody 不会消费 body，之后读取 response.body() 时数据仍然有效
            String bodyString = response.peekBody(Long.MAX_VALUE).string();
            ApiResponse<?> apiResponse;
            try {
                apiResponse = new Gson().fromJson(bodyString, ApiResponse.class);
            } catch (JsonParseException e) {
                throw NetworkErrorMapper.fromThrowable(e, request);
            }

            if (apiResponse != null && !apiResponse.isSuccess()) {
                int bizCode = apiResponse.getCode();
                String bizMsg = apiResponse.getMessage();

                // 401 业务码也需要处理（如 Token 过期但 HTTP 状态码仍是 200）
                if (bizCode == 401) {
                    postTokenExpiredIfNeeded(request);
                }

                throw NetworkErrorMapper.fromBusinessCode(bizCode, bizMsg, request);
            }

            // 业务成功，原样返回 response
            return response;
        }
    }
}
