package com.ggg.rememo.core.base;

import java.lang.ref.WeakReference;

/**
 * MVP 架构中 Presenter 层的基抽象类。
 * <p>
 * 所有 MVP 页面的 Presenter 都应继承此抽象类。
 * 负责管理 View 的生命周期，防止内存泄漏。
 * </p>
 *
 * @param <V> 关联的 View 类型，必须实现 {@link IBaseView} 接口
 */
public abstract class BasePresenter<V extends IBaseView> {

    /**
     * View 的弱引用，使用弱引用避免内存泄漏。
     */
    private WeakReference<V> viewRef;
    /**
     * 绑定 View，在 Fragment.onCreateView 或 Activity.onCreate 中调用。
     *
     * @param view View 实例
     */
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
        onViewAttached();
    }

    /**
     * 解绑 View，在 Fragment.onDestroyView 或 Activity.onDestroy 中调用。
     * 调用后 View 引用将被清除。
     */
    public void detachView() {
        onViewDetached();
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    /**
     * 获取绑定的 View 实例。
     * <p>
     * 建议优先使用 {@link #ifViewAttached(ViewCallback)} 方法进行空检查和操作。
     * </p>
     *
     * @return View 实例，如果已解绑则返回 null
     */
    protected V getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    /**
     * 检查 View 是否已绑定且未解绑。
     *
     * @return true 表示 View 可用
     */
    protected boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    /**
     * 安全执行 View 操作，自动进行空检查。
     * <p>
     * 推荐使用此方法代替直接调用 View 方法，避免空指针异常。
     * </p>
     * <p>
     * 示例：
     * <pre>
     * ifViewAttached(new ViewCallback&lt;V&gt;() {
     *     {@literal @}Override
     *     public void call(V view) {
     *         view.showLoading();
     *         view.showData(data);
     *     }
     * });
     * </pre>
     * </p>
     *
     * @param callback 需要执行的 View 操作回调
     */
    protected void ifViewAttached(ViewCallback<V> callback) {
        V view = getView();
        if (view != null) {
            callback.call(view);
        }
    }


    // 钩子方法，子类可覆盖实现自定义逻辑
    protected void onViewAttached() {

    }

    protected void onViewDetached() {

    }

    /**
     * 获取 View 实例的快捷方法（不带空检查）。
     * <p>
     * 仅推荐在确定 View 一定存在的情况下使用，
     * 大多数场景请使用 {@link #ifViewAttached(ViewCallback)} 方法。
     * </p>
     *
     * @return View 实例
     * @throws IllegalStateException 如果 View 未绑定
     */
    protected V requireView() {
        V view = getView();
        if (view == null) {
            throw new IllegalStateException("View is not attached. Call attachView() first.");
        }
        return view;
    }

    /**
     * View 回调接口，用于 {@link #ifViewAttached(ViewCallback)} 方法。
     * <p>
     * 使用此接口替代 Java 8 的 Consumer，避免 Android Java 8 兼容性问题。
     * </p>
     *
     * @param <V> View 类型
     */
    public interface ViewCallback<V> {
        void call(V view);
    }
}
