package com.ggg.rememo.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * MVP 架构中 Fragment 的基类。
 * <p>
 * 自动管理 Presenter 的生命周期（attach/detach），
 * 子类只需关注业务逻辑的实现。
 * </p>
 *
 * @param <VB> ViewBinding 类型
 * @param <V>  View 接口类型，必须实现 {@link IBaseView}
 * @param <P>  Presenter 类型，必须继承 {@link BasePresenter}
 */
public abstract class BaseFragment<VB extends ViewBinding, V extends IBaseView, P extends BasePresenter<V>>
        extends Fragment implements IBaseView {

    /**
     * Presenter 实例，由子类通过 {@link #createPresenter()} 创建
     */
    protected P presenter;
    private VB binding;


    /**
     * 子类通过此方法获取 Binding 实例，确保空安全
     */
    @NonNull
    protected final VB getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Fragment " + this + " 视图已销毁或尚未创建，禁止访问 Binding");
        }
        return binding;
    }

    /**
     * 子类必须实现：提供 ViewBinding 的 inflate 逻辑
     * 示例：return FragmentExampleBinding.inflate(inflater, container, false);
     */
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    /**
     * 创建 Presenter 实例。
     * <p>
     * 子类必须实现此方法以提供具体的 Presenter 实例。
     * </p>
     *
     * @return Presenter 实例
     */
    protected abstract P createPresenter();

    protected abstract V getViewContract();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 创建并绑定 Presenter
        if (presenter == null) {
            presenter = createPresenter();
        }
        if (presenter != null) {
            presenter.attachView(getViewContract());
        }

        initView();
        initData();
    }
    /** 子类初始化视图 */
    protected void initView() {}
    /** 子类初始化数据 */
    protected void initData() {}

    @Override
    public void onDestroyView() {
        // 解绑 Presenter
        if (presenter != null) {
            presenter.detachView();
        }
        // 释放 binding 引用
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter = null;
    }

    /**
     * 检查 Presenter 是否已绑定。
     */
    protected boolean isPresenterAttached() {
        return presenter != null;
    }

    /**
     * 检查 Fragment 是否处于可视状态。
     */
    protected final boolean isUIActive() {
        return isAdded() && !isDetached() && getView() != null;
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
