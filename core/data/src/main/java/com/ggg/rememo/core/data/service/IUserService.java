package com.ggg.rememo.core.data.service;

import com.ggg.rememo.core.data.model.network.response.UserInfo;

/**
 * 用户信息服务接口。
 * 通过 ARouter 调用，实现模块间解耦。
 */
public interface IUserService {

    /**
     * 用户信息回调。
     */
    interface UserInfoCallback {
        void onSuccess(UserInfo userInfo);
        void onError(String message);
    }

    /**
     * 通用回调。
     */
    interface Callback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * 获取当前用户信息（从网络）。
     */
    void getUserInfo(UserInfoCallback callback);

    /**
     * 获取本地缓存的用户信息。
     */
    void getLocalUserInfo(UserInfoCallback callback);

    /**
     * 保存用户信息到本地数据库。
     */
    void saveUserInfo(UserInfo userInfo, Callback callback);

    /**
     * 更新用户资料到服务端。
     */
    void updateProfile(String nickname, String avatar, String gender, String bio,
                       UserInfoCallback callback);

    /**
     * 是否需要刷新用户信息（用于通知其他页面刷新）。
     */
    boolean shouldRefresh();

    /**
     * 标记需要刷新。
     */
    void markNeedsRefresh();

    /**
     * 清除刷新标记。
     */
    void clearRefreshFlag();
}
