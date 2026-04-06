package com.ggg.rememo.feature.explore.Fragment;


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
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.explore.Adapter.ExplorePageAdapter;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreHomeBinding;
import com.ggg.rememo.feature.explore.presenter.ExplorePresenter;
import com.google.android.material.tabs.TabLayoutMediator;

@Route(path = Routes.Explore.HOME_FRAGMENT)
public class ExploreHomeFragment extends Fragment implements RecFragment.OnParentAttach, NearbyFragment.OnParentAttach {

    private FragmentExploreHomeBinding binding;
    private ExplorePageAdapter adapter;
    private ExplorePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentExploreHomeBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initPresenter();
        initViewPager();
    }

    private void initPresenter() {
        presenter = new ExplorePresenter();
    }

    private void initViewPager() {
        adapter = new ExplorePageAdapter(this);
        binding.exploreHomeViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.exploreHomeViewPager2.setAdapter(adapter);

        new TabLayoutMediator(binding.exploreHomeTabLayout, binding.exploreHomeViewPager2,
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
