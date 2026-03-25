package com.ggg.rememo.core.network;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络客户端单例。
 * 负责初始化 OkHttpClient 和 Retrofit 实例。
 */
public class NetworkClient {

    // Android 模拟器访问本机后端时使用 10.0.2.2
    // TODO: 上线前改为真实服务器地址
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
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    /**
     * 自动注入 Authorization Header 的拦截器。
     * 从 NetworkClient.setAuthToken() 读取 Token 并注入到所有请求中。
     */
    private static class AuthInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            if (authToken != null && !authToken.isEmpty()) {
                Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json");
                return chain.proceed(builder.build());
            }

            return chain.proceed(original);
        }
    }
}
