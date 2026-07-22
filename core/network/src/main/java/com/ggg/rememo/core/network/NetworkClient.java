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


    private OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.redactHeader("Authorization");
        loggingInterceptor.redactHeader("Cookie");
        loggingInterceptor.setLevel(
                NetworkGovernancePolicy.loggingLevel(BuildConfig.DEBUG)
        );

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(new ApiResponseInterceptor())
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

            if (NetworkGovernancePolicy.isAuthRequest(original)) {
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

}
