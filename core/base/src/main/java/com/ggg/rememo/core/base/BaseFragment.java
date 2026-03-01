package com.ggg.rememo.core.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * MVP 架构中 Fragment 的基类。
 * <p>
 * 自动管理 Presenter 的生命周期（attach/detach），
 * 子类只需关注业务逻辑的实现。
 * </p>
 *
 * @param <V> View 接口类型，必须实现 {@link BaseView}
 * @param <P> Presenter 类型，必须继承 {@link BasePresenter}
 */
public abstract class BaseFragment<V extends BaseView, P extends BasePresenter<V>>
        extends Fragment implements BaseView {

    /**
     * Presenter 实例，由子类通过 {@link #createPresenter()} 创建
     */
    protected P presenter;

    /**
     * 创建 Presenter 实例。
     * <p>
     * 子类必须实现此方法以提供具体的 Presenter 实例。
     * </p>
     *
     * @return Presenter 实例
     */
    protected abstract P createPresenter();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 创建并绑定 Presenter
        presenter = createPresenter();
        if (presenter != null) {
            // 子类正确声明泛型 V 时，此类型转换是安全的
            @SuppressWarnings("unchecked")
            V viewImpl = (V) this;
            presenter.attachView(viewImpl);
        }
    }

    @Override
    public void onDestroyView() {
        // 解绑 Presenter，防止内存泄漏
        if (presenter != null) {
            presenter.detachView();
            presenter = null;
        }
        super.onDestroyView();
    }

    /**
     * 检查 Presenter 是否已绑定。
     *
     * @return true 表示 Presenter 可用
     */
    protected boolean isPresenterAttached() {
        return presenter != null;
    }

    /**
     * 安全执行 Presenter 操作，自动进行空检查。
     * <p>
     * 推荐使用此方法代替直接调用 Presenter 方法。
     * </p>
     * <p>
     * 示例：
     * <pre>
     * ifPresenterAttached(new PresenterCallback&lt;P&gt;() {
     *     {@literal @}Override
     *     public void call(P presenter) {
     *         presenter.loadData();
     *     }
     * });
     * </pre>
     * </p>
     *
     * @param callback 需要执行的 Presenter 操作回调
     */
    protected void ifPresenterAttached(PresenterCallback<P> callback) {
        if (presenter != null) {
            callback.call(presenter);
        }
    }

    // ========== BaseView 默认实现 ==========

    @Override
    public void showLoading() {
        // 默认空实现，子类可覆盖
    }

    @Override
    public void hideLoading() {
        // 默认空实现，子类可覆盖
    }

    @Override
    public void showError(String message) {
        // 默认空实现，子类可覆盖
    }

    /**
     * Presenter 回调接口，用于 {@link #ifPresenterAttached(PresenterCallback)} 方法。
     * <p>
     * 使用此接口避免 Android Java 8 兼容性问题。
     * </p>
     *
     * @param <P> Presenter 类型
     */
    protected interface PresenterCallback<P> {
        void call(P presenter);
    }
}
