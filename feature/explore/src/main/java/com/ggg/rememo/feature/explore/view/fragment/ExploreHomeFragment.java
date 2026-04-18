package com.ggg.rememo.feature.explore.view.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.base.BaseFragment;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.explore.contract.ExploreContract;
import com.ggg.rememo.feature.explore.view.adapter.ExplorePageAdapter;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreHomeBinding;
import com.ggg.rememo.feature.explore.presenter.ExplorePresenter;
import com.google.android.material.tabs.TabLayoutMediator;

@Route(path = Routes.Explore.HOME_FRAGMENT)
public class ExploreHomeFragment extends BaseFragment<
        FragmentExploreHomeBinding,
        ExploreContract.HomeView,
        ExplorePresenter>
        implements ExploreContract.HomeView, RecFragment.OnParentAttach, NearbyFragment.OnParentAttach {
    private ExplorePageAdapter adapter;

    @Override
    protected FragmentExploreHomeBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentExploreHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected ExplorePresenter createPresenter() {
        return new ExplorePresenter();
    }

    @Override
    protected ExploreContract.HomeView getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        initViewPager();
    }

    private void initViewPager() {
        adapter = new ExplorePageAdapter(this);
        getBinding().exploreHomeViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        getBinding().exploreHomeViewPager2.setAdapter(adapter);

        new TabLayoutMediator(getBinding().exploreHomeTabLayout, getBinding().exploreHomeViewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("推荐");
                            break;
                        case 1:
                            tab.setText("附近");
                            break;
                        default:
                            tab.setText("未知");
                            break;
                    }
                }).attach();
    }

    @Override
    public void onRecViewAttached(RecFragment fragment) {
        fragment.setPresenter(presenter);
        presenter.attachRecView(fragment);
    }

    @Override
    public void onNearbyViewAttached(NearbyFragment fragment) {
        fragment.setPresenter(presenter);
        presenter.attachNearbyView(fragment);
    }
}
