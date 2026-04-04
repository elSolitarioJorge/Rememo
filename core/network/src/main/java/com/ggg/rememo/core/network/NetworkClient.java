package com.ggg.rememo.core.network;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.common.util.TokenManager;

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

    private static final String BASE_URL = "http://192.168.1.37:9090/";

    private static volatile NetworkClient instance;
    private static volatile String authToken;

    private final Retrofit retrofit;
    private final ApiService apiService;

    private NetworkClient() {
        OkHttpClient okHttpClient = buildOkHttpClient();

        retrofit = new Retrofit.Builder()
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

    /**
     * 设置全局认证 Token。
     * 在登录/注册成功后由调用方传入，后续所有请求自动携带此 Token。
     *
     * @param token Bearer Token
     */
    public static void setAuthToken(String token) {
        authToken = token;
    }

    /**
     * 清除认证 Token。
     * 在退出登录时调用。
     */
    public static void clearAuthToken() {
        authToken = null;
    }

    private OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
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

            String token = TokenManager.getToken();
            if (token != null && !token.isEmpty()) {
                android.util.Log.d("AuthInterceptor", "[Auth] Token 已注入: " + token.substring(0, Math.min(10, token.length())) + "...");
                Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + token);
                return chain.proceed(builder.build());
            }

            android.util.Log.w("AuthInterceptor", "[Auth] Token 为空! 请求 " + original.url() + " 将不带 Authorization Header，可能导致 401");
            return chain.proceed(original);
        }
    }
}
