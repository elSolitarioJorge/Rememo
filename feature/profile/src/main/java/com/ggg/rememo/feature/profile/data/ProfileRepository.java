package com.ggg.rememo.feature.profile.data;

import android.util.Log;
import androidx.annotation.NonNull;

import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.model.network.request.UpdateUserRequest;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.data.repository.UserRepository;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile 模块数据仓库。
 * 负责用户信息相关的网络请求与本地持久化。
 */
public class ProfileRepository {

    private static final String TAG = "ProfileRepository";
    private final ApiService apiService;
    private final UserRepository userRepository;

    public ProfileRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
        this.userRepository = new UserRepository();
    }

    /**
     * 从服务端获取当前用户信息。
     */
    public void getUserInfo(ApiCallback<UserInfo> callback) {
        apiService.getUserInfo().enqueue(new Callback<ApiResponse<UserInfo>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserInfo>> call,
                                   @NonNull Response<ApiResponse<UserInfo>> response) {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserInfo>> call, @NonNull Throwable t) {
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 更新用户资料到服务端。
     */
    public void updateProfile(String nickname, String avatar, String gender, String bio,
                               ApiCallback<UserInfo> callback) {
        UpdateUserRequest request = new UpdateUserRequest(nickname, avatar, gender, bio);
        Log.d(TAG, "updateProfile 请求 - nickname=" + nickname + ", avatar长度=" + (avatar == null ? 0 : avatar.length()) + ", gender=" + gender + ", bio=" + bio);
        Log.d(TAG, "updateProfile 请求体: " + new Gson().toJson(request));
        apiService.updateUserInfo(request).enqueue(new Callback<ApiResponse<UserInfo>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserInfo>> call,
                                   @NonNull Response<ApiResponse<UserInfo>> response) {
                Log.d(TAG, "updateProfile 响应 - code=" + response.code() + ", body=" + response.body());
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserInfo>> call, @NonNull Throwable t) {
                Log.e(TAG, "updateProfile 请求失败", t);
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 将 UserInfo 缓存到本地数据库。
     */
    public void saveUserInfo(UserInfo userInfo, ApiCallback<Void> callback) {
        User user = new User();
        user.setUserId(userInfo.getUserId());
        user.setNickname(userInfo.getNickname());
        user.setAvatar(userInfo.getAvatar());
        user.setGender(userInfo.getGender());
        user.setBio(userInfo.getBio());
        userRepository.insert(user, new UserRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * 从本地数据库获取缓存的用户信息。
     */
    public void getLocalUser(ApiCallback<User> callback) {
        userRepository.getById(
                TokenManager.getUserId(),
                new UserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        if (callback != null) {
                            callback.onSuccess(result);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    }
                }
        );
    }

    private <T> void handleResponse(Response<ApiResponse<T>> response, ApiCallback<T> callback) {
        Log.d(TAG, "handleResponse HTTP code=" + response.code());
        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<T> body = response.body();
            Log.d(TAG, "handleResponse body -> code=" + body.getCode() + ", message=" + body.getMessage() + ", data=" + body.getData());
            if (body.isSuccess()) {
                callback.onSuccess(body.getData());
            } else {
                callback.onError(body.getMessage());
            }
        } else {
            String errorBody = "";
            try {
                if (response.errorBody() != null) errorBody = response.errorBody().string();
            } catch (Exception ignored) {}
            Log.e(TAG, "handleResponse 请求失败: code=" + response.code() + ", errorBody=" + errorBody);
            callback.onError("请求失败: " + response.code());
        }
    }
}
