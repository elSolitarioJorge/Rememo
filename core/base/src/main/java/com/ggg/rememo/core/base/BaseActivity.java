package com.ggg.rememo.core.base;


import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

/**
 * MVP 架构中 Activity 的基类。
 * <p>
 * 自动管理 Presenter 的生命周期（attach/detach），
 * 子类只需关注业务逻辑的实现。
 * </p>
 *
 * @param <VB> ViewBinding 类型
 * @param <V>  View 接口类型，必须实现 {@link IBaseView}
 * @param <P>  Presenter 类型，必须继承 {@link BasePresenter}
 */
public abstract class BaseActivity<VB extends ViewBinding, V extends IBaseView, P extends BasePresenter<V>>
        extends AppCompatActivity implements IBaseView {

    protected P presenter;
    private VB binding;
    @NonNull
    protected final VB getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Activity " + this + " 尚未创建或已销毁，禁止访问 Binding");
        }
        return binding;
    }
    /** 子类必须实现 ViewBinding 的 inflate 逻辑 */
    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater);
    /** 子类必须实现，提供 Presenter 实例 */
    protected abstract P createPresenter();
    /** 子类必须实现，返回 View Contract */
    protected abstract V getViewContract();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets();
        if (presenter == null) {
            presenter = createPresenter();
        }
        if (presenter != null) {
            presenter.attachView(getViewContract());
        }
        initView();
        initData();
    }
    protected void initView() {}
    protected void initData() {}

    /**
     * 默认的系统栏适配逻辑
     * 自动为根布局添加 padding，防止内容被状态栏或导航栏遮挡。
     * 如果子类有特殊需求（比如背景图沉浸，但按钮不沉浸），可以重写此方法。
     */
    protected void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        binding = null;
        super.onDestroy();
        presenter = null;
    }
    protected boolean isPresenterAttached() {
        return presenter != null;
    }
    protected void ifPresenterAttached(PresenterCallback<P> callback) {
        if (presenter != null) {
            callback.call(presenter);
        }
    }
    protected interface PresenterCallback<P> {
        void call(P presenter);
    }
}
