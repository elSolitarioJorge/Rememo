package com.ggg.rememo.feature.detail.contract;

import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPost;

/**
 * 记忆详情模块 MVP Contract。
 */
public interface MemoryDetailContract {

    /**
     * View 接口。
     */
    interface View extends BaseView {

        /**
         * 展示记忆详情。
         */
        void showMemory(MemoryPost post);

        /**
         * 刷新指定图片的 AI 修复状态。
         */
        void notifyImageFixed(int position);

        /**
         * 更新点赞状态和数量。
         * @param isLiked    当前用户是否已点赞
         * @param likeCount  最新的点赞数量
         */
        void updateLikeState(boolean isLiked, int likeCount);

        /**
         * 更新收藏状态和数量。
         * @param isCollected  当前用户是否已收藏
         * @param collectCount  最新的收藏数量
         */
        void updateCollectState(boolean isCollected, int collectCount);

        /**
         * 显示 Toast 提示。
         */
        void showToast(String message);
    }

    /**
     * Presenter 接口。
     */
    interface Presenter {

        /**
         * 加载记忆详情。
         */
        void loadMemory(String postId);

        /**
         * 分享按钮点击。
         */
        void onShareClick();

        /**
         * 点赞按钮点击。
         */
        void onLikeClick();

        /**
         * 收藏按钮点击。
         */
        void onStarClick();

        /**
         * AI 修复按钮点击。
         */
        void onImageAiFix(int position);
    }
}
