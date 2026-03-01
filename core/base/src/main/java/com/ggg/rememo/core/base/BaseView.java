package com.ggg.rememo.core.base;

/**
 * MVP 架构中 View 层的基接口。
 * <p>
 * 所有 MVP 页面的 View 接口都应继承此接口。
 * 提供基础的 UI 状态回调方法。
 * </p>
 */
public interface BaseView {

    /**
     * 显示加载中状态。
     * 子类可根据需要覆盖此方法实现自定义加载 UI。
     */
    default void showLoading() {
        // 默认空实现，由子类覆盖
    }

    /**
     * 隐藏加载中状态。
     * 子类可根据需要覆盖此方法实现自定义加载 UI。
     */
    default void hideLoading() {
        // 默认空实现，由子类覆盖
    }

    /**
     * 显示错误信息。
     * 子类可根据需要覆盖此方法实现自定义错误展示（如 Toast、Snackbar、Dialog）。
     *
     * @param message 错误信息内容
     */
    default void showError(String message) {
        // 默认空实现，由子类覆盖
    }
}
