package com.ggg.rememo.feature.timeline.contract;

import android.content.Intent;

import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.feature.timeline.model.TimelineYearModel;

import java.util.List;

/**
 * 时间线模块 MVP 契约接口
 */
public interface TimelineContract {

    /**
     * View 接口
     */
    interface View extends BaseView {
        /**
         * 显示时间线数据
         * @param timelineYears 按年份分组的时间线数据
         */
        void showTimeline(List<TimelineYearModel> timelineYears);

        /**
         * 显示空状态
         */
        void showEmpty();

        /**
         * 显示地点标题
         * @param title 标题
         * @param subtitle 副标题/地点名
         */
        void showHeader(String title, String subtitle);

        /**
         * 刷新时间线数据（由网络刷新触发，仅更新数据不显示 loading）
         * @param timelineYears 最新时间线数据
         */
        void refreshTimeline(List<TimelineYearModel> timelineYears);

        /**
         * 跳转到记忆详情页
         * @param postId 记忆ID
         */
        void navigateToMemoryDetail(String postId);

        /**
         * 跳转到发布页面
         * @param pointId 地点ID
         * @param lat 纬度（可选，0 表示使用当前位置）
         * @param lng 经度（可选，0 表示使用当前位置）
         * @param address 地址（可选）
         * @param pointName 地址（可选）
         */
        void navigateToPublish(String pointId, double lat, double lng, String address, String pointName);

        /**
         * 跳转系统分享
         * @param shareIntent 分享意图
         */
        void navigateToShare(Intent shareIntent);

        /**
         * 跳转年份探索页（时空传送门/探索更多入口）
         *
         * @param year         年份
         * @param memoryCount  该年份记忆总数
         */
        void navigateToExploreYear(int year, int memoryCount);
    }

    /**
     * Presenter 接口
     */
    interface Presenter {
        /**
         * 加载时间线数据
         * @param pointId 地点ID
         */
        void loadTimeline(String pointId);

        /**
         * 记忆卡片点击
         * @param postId 记忆ID
         */
        void onMemoryClicked(String postId);

        /**
         * 添加记忆按钮点击
         */
        void onAddMemoryClicked();

        /**
         * 分享按钮点击
         */
        void onShareClicked();

        /**
         * 时空传送门/探索更多按钮点击（年份维度入口）
         *
         * @param yearModel 被点击的年份数据
         */
        void onGatewayClick(TimelineYearModel yearModel);
    }
}
