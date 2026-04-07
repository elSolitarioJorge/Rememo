package com.ggg.rememo.feature.profile.contract;

import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.UserInfo;

import java.util.List;

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
         * 使用更新后的用户信息立即刷新主页（避免重新请求网络）。
         *
         * @param userInfo 更新后的用户信息
         */
        default void showUpdateSuccessWithData(UserInfo userInfo) {
            // 默认实现兼容旧逻辑
            showUpdateSuccess();
        }

        /**
         * 退出登录后跳转到登录页。
         */
        void navigateToLogin();

        /**
         * 展示用户记忆列表。
         *
         * @param posts 记忆列表（已按 createdTime 倒序）
         */
        void showMemories(List<MemoryPost> posts);

        /**
         * 展示空数据状态。
         */
        void showMemoriesEmpty();

        /**
         * 跳转到记忆详情页。
         *
         * @param postId 记忆 ID
         */
        void navigateToMemoryDetail(String postId);
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
         * 直接显示用户信息（不触发网络请求，用于编辑成功后刷新）。
         */
        default void showUserInfoDirectly(UserInfo userInfo) {
        }

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

        /**
         * 加载当前用户的记忆列表（按用户ID）。
         */
        void loadUserMemories();

        /**
         * 处理记忆卡片点击，跳转到详情页。
         *
         * @param postId 记忆 ID
         */
        default void onMemoryClicked(String postId) {
        }
    }
}
