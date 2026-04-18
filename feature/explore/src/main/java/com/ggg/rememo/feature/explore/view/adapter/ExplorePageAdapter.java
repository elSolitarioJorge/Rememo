package com.ggg.rememo.feature.explore.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ggg.rememo.feature.explore.view.fragment.ExploreHomeFragment;
import com.ggg.rememo.feature.explore.view.fragment.NearbyFragment;
import com.ggg.rememo.feature.explore.view.fragment.RecFragment;

public class ExplorePageAdapter extends FragmentStateAdapter {

    public ExplorePageAdapter(@NonNull ExploreHomeFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RecFragment();
            case 1:
                return new NearbyFragment();
            default:
                return new RecFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
