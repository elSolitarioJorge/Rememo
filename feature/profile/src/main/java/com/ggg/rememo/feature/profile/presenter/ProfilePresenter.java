package com.ggg.rememo.feature.profile.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.data.ProfileRepository;

/**
 * Profile 模块 Presenter。
 * 职责：加载用户信息、保存编辑资料、退出登录。
 */
public class ProfilePresenter extends BasePresenter<ProfileContract.View>
        implements ProfileContract.Presenter {

    private static final String TAG = "ProfilePresenter";
    private final ProfileRepository repository;
    private final Handler mainHandler;

    public ProfilePresenter() {
        this.repository = new ProfileRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadUserInfo() {
        ifViewAttached(view -> view.showLoading());

        repository.getUserInfo(new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo data) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showUserInfo(data);
                    });
                    repository.saveUserInfo(data, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            // 缓存本地成功，无额外操作
                        }

                        @Override
                        public void onError(String message) {
                            // 缓存失败不影响主流程，静默处理
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "loadUserInfo 失败: " + message);
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showError(message);
                    });
                });
            }
        });
    }

    @Override
    public void updateProfile(String nickname, String avatar, String gender, String bio) {
        Log.d(TAG, "updateProfile 调用: nickname=" + nickname + ", avatar长度=" + (avatar == null ? 0 : avatar.length()) + ", gender=" + gender + ", bio=" + bio);
        ifViewAttached(view -> view.showLoading());

        repository.updateProfile(nickname, avatar, gender, bio, new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo data) {
                Log.d(TAG, "updateProfile 成功: " + data);
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showUpdateSuccess();
                    });
                    repository.saveUserInfo(data, new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "updateProfile 失败: " + message);
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showError(message);
                    });
                });
            }
        });
    }

    @Override
    public void logout() {
        TokenManager.logout();
        NetworkClient.clearAuthToken();
        ifViewAttached(view -> view.navigateToLogin());
    }
}
