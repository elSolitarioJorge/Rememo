package com.ggg.rememo.core.network;

import com.ggg.rememo.core.data.model.network.response.AuthResponse;
import com.ggg.rememo.core.data.model.network.request.LoginRequest;
import com.ggg.rememo.core.data.model.network.request.RegisterRequest;
import com.ggg.rememo.core.data.model.network.response.UserInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Retrofit API 服务接口定义。
 * 定义所有业务接口的网络请求方法。
 */
public interface ApiService {

    // ==================== 认证模块 ====================

    /**
     * 密码登录
     */
    @POST("/api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    /**
     * 用户注册
     */
    @POST("/api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    // ==================== 用户模块 ====================

    /**
     * 获取当前用户信息
     */
    @GET("/api/user/info")
    Call<ApiResponse<UserInfo>> getUserInfo();

    /**
     * 更新用户信息（昵称、头像）
     */
    @PUT("/api/user/info")
    Call<ApiResponse<UserInfo>> updateUserInfo(@Body Object request);
}
