package com.ggg.rememo.feature.auth.data;


import androidx.annotation.NonNull;

import com.ggg.rememo.core.data.model.network.response.AuthResponse;
import com.ggg.rememo.core.data.model.network.request.LoginRequest;
import com.ggg.rememo.core.data.model.network.request.RegisterRequest;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认证模块数据仓库。
 * 负责所有网络请求的发起与回调转换。
 */
public class AuthRepository {

    private final ApiService apiService;

    public AuthRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
    }

    /**
     * 密码登录
     */
    public void login(String phone, String password, ApiCallback<AuthResponse> callback) {
        LoginRequest request = new LoginRequest(phone, password);
        apiService.login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 用户注册
     */
    public void register(String phone, String password, ApiCallback<AuthResponse> callback) {
        RegisterRequest request = new RegisterRequest(phone, password);
        apiService.register(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 发送登录验证码（占位，后端接口未定义时使用）
     */
    public void sendLoginCode(String phone, ApiCallback<Void> callback) {
        // TODO: 后端接口定义后替换为真实接口
        // mock: 模拟成功
        callback.onSuccess(null);
    }

    /**
     * 发送注册验证码（占位，后端接口未定义时使用）
     */
    public void sendRegisterCode(String phone, ApiCallback<Void> callback) {
        // TODO: 后端接口定义后替换为真实接口
        // mock: 模拟成功
        callback.onSuccess(null);
    }

    private <T> void handleResponse(Response<ApiResponse<T>> response, ApiCallback<T> callback) {
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> body = response.body();
            if (body.isSuccess()) {
                callback.onSuccess(body.getData());
            } else {
                callback.onError(body.getMessage());
            }
        } else {
            callback.onError("请求失败: " + response.code());
        }
    }
}
