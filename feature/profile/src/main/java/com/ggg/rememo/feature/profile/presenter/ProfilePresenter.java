package com.ggg.rememo.feature.profile.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.data.ProfileRepository;

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

        // 立即从本地缓存加载（同步），避免闪烁
        repository.getLocalUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User localUser) {
                if (localUser != null) {
                    UserInfo userInfo = convertToUserInfo(localUser);
                    mainHandler.post(() -> {
                        ifViewAttached(view -> {
                            view.hideLoading();
                            view.showUserInfo(userInfo);
                        });
                    });
                }
                // 后台静默同步网络数据（仅当有变化时更新）
                syncFromNetwork();
            }

            @Override
            public void onError(String message) {
                // 本地无缓存，直接从网络加载
                syncFromNetwork();
            }
        });
    }

    /**
     * 后台静默同步网络数据。
     * 只在数据有变化时更新本地缓存和 UI。
     */
    private void syncFromNetwork() {
        repository.getUserInfo(new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo networkData) {
                // 检查数据是否有变化（getLocalUser 已在子线程，这里需要 post 到主线程）
                repository.getLocalUser(new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User localUser) {
                        boolean hasChanges = localUser == null ||
                                !equalsOrBothEmpty(localUser.getNickname(), networkData.getNickname()) ||
                                !equalsOrBothEmpty(localUser.getAvatar(), networkData.getAvatar()) ||
                                !equalsOrBothEmpty(localUser.getGender(), networkData.getGender()) ||
                                !equalsOrBothEmpty(localUser.getBio(), networkData.getBio());

                        mainHandler.post(() -> {
                            if (hasChanges) {
                                // 数据有变化，更新 UI 和本地缓存
                                ifViewAttached(view -> {
                                    view.hideLoading();
                                    view.showUserInfo(networkData);
                                });
                                saveToLocalCache(networkData);
                            } else {
                                ifViewAttached(view -> view.hideLoading());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        // 本地读取失败，仍显示网络数据
                        mainHandler.post(() -> {
                            ifViewAttached(view -> {
                                view.hideLoading();
                                view.showUserInfo(networkData);
                            });
                            saveToLocalCache(networkData);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.d(TAG, "syncFromNetwork 网络同步失败: " + message);
                // 网络失败时，只要之前已显示本地数据就不报错
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.hideLoading());
                });
            }
        });
    }

    private boolean equalsOrBothEmpty(String a, String b) {
        String aa = a == null ? "" : a;
        String bb = b == null ? "" : b;
        return aa.equals(bb);
    }

    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setBio(user.getBio());
        userInfo.setCreatedAt(user.getCreatedAt());
        return userInfo;
    }

    private void saveToLocalCache(UserInfo userInfo) {
        repository.saveUserInfo(userInfo, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    @Override
    public void updateProfile(String nickname, String avatar, String gender, String bio) {
        Log.d(TAG, "updateProfile 调用: nickname=" + nickname + ", avatar=" + (avatar == null ? "null" : avatar.substring(0, Math.min(50, avatar.length())) + "...") + ", gender=" + gender + ", bio=" + bio);
        ifViewAttached(view -> view.showLoading());

        repository.updateProfile(nickname, avatar, gender, bio, new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo data) {
                Log.d(TAG, "updateProfile 成功，直接更新 UI 和本地缓存");
                mainHandler.post(() -> {
                    // 保存到本地缓存
                    saveToLocalCache(data);
                    // 直接使用新数据更新 UI，避免重新请求
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showUpdateSuccessWithData(data);
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

    /**
     * 先上传头像图片，获取 URL 后再更新用户信息。
     *
     * @param imagePath 本地头像图片路径
     * @param nickname  昵称
     * @param gender    性别
     * @param bio       个人简介
     */
    public void uploadAvatarAndUpdateProfile(String imagePath, String nickname, String gender, String bio) {
        Log.d(TAG, "uploadAvatarAndUpdateProfile: 先上传头像 imagePath=" + imagePath);
        ifViewAttached(view -> view.showLoading());

        repository.uploadAvatarImage(imagePath, new ApiCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "头像上传成功，URL=" + imageUrl + "，开始更新用户信息");
                // 上传成功后，用返回的 URL 更新用户信息
                repository.updateProfile(nickname, imageUrl, gender, bio, new ApiCallback<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo data) {
                        Log.d(TAG, "用户信息更新成功");
                        mainHandler.post(() -> {
                            saveToLocalCache(data);
                            ifViewAttached(view -> {
                                view.hideLoading();
                                view.showUpdateSuccessWithData(data);
                            });
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "用户信息更新失败: " + message);
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
            public void onError(String message) {
                Log.e(TAG, "头像上传失败: " + message);
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showError("头像上传失败: " + message);
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

    @Override
    public void showUserInfoDirectly(UserInfo userInfo) {
        mainHandler.post(() -> {
            ifViewAttached(view -> view.showUserInfo(userInfo));
        });
    }
}
