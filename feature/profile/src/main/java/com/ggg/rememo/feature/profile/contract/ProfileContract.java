package com.ggg.rememo.feature.profile.contract;

import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.core.data.model.network.response.UserInfo;

/**
 * Profile 模块 MVP Contract。
 * 包含主页展示、编辑资料保存、退出登录三大功能的 View/Presenter 接口定义。
 */
public interface ProfileContract {

    /**
     * Profile 模块 View 接口。
     * 由 ProfileHomeFragment、EditProfileDialogFragment、SettingsDialogFragment 共同实现。
     */
    interface View extends BaseView {

        /**
         * 展示用户信息到主页。
         *
         * @param userInfo 用户信息
         */
        void showUserInfo(UserInfo userInfo);

        /**
         * 保存成功后关闭编辑弹窗并刷新主页。
         */
        void showUpdateSuccess();

        /**
         * 退出登录后跳转到登录页。
         */
        void navigateToLogin();
    }

    /**
     * Profile 模块 Presenter 接口。
     */
    interface Presenter {

        /**
         * 加载用户信息到主页。
         */
        void loadUserInfo();

        /**
         * 保存编辑后的资料。
         *
         * @param nickname 昵称
         * @param avatar   头像 URL
         * @param gender   性别 male / female / secret
         * @param bio      签名
         */
        void updateProfile(String nickname, String avatar, String gender, String bio);

        /**
         * 退出登录。
         */
        void logout();
    }
}
