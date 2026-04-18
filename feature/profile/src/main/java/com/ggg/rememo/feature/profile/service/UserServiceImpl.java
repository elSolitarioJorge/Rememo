package com.ggg.rememo.feature.profile.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.data.repository.UserRepository;
import com.ggg.rememo.core.data.service.IUserService;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.feature.profile.data.ProfileRepository;

/**
 * 用户信息服务实现。
 * 通过 ARouter 暴露给其他模块。
 */
@Route(path = Routes.Profile.USER_SERVICE)
public class UserServiceImpl implements IUserService, IProvider {

    private static final String TAG = "UserServiceImpl";
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final Handler mainHandler;
    private volatile boolean needsRefresh = false;

    public UserServiceImpl() {
        this.profileRepository = new ProfileRepository();
        this.userRepository = new UserRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void init(Context context) {
        // ARouter 服务初始化
    }

    @Override
    public void getUserInfo(UserInfoCallback callback) {
        profileRepository.getUserInfo(new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void getLocalUserInfo(UserInfoCallback callback) {
        userRepository.getById(
                TokenManager.getUserId(),
                new UserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        if (result != null) {
                            callback.onSuccess(convertToUserInfo(result));
                        } else {
                            callback.onError("本地无缓存");
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
        );
    }

    @Override
    public void saveUserInfo(UserInfo userInfo, Callback callback) {
        profileRepository.saveUserInfo(userInfo, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void updateProfile(String nickname, String avatar, String gender, String bio,
                             UserInfoCallback callback) {
        profileRepository.updateProfile(nickname, avatar, gender, bio, new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public boolean shouldRefresh() {
        return needsRefresh;
    }

    @Override
    public void markNeedsRefresh() {
        this.needsRefresh = true;
    }

    @Override
    public void clearRefreshFlag() {
        this.needsRefresh = false;
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
}
