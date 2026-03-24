package com.ggg.rememo.core.data.model.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * 登录/注册成功响应数据。
 */
public class AuthResponse {

    @SerializedName("userId")
    private String userId;

    @SerializedName("token")
    private String token;

    @SerializedName("expiresAt")
    private long expiresAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
