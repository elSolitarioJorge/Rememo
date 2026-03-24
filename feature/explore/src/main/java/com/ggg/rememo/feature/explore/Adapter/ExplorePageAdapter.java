package com.ggg.rememo.feature.explore.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.Fragment.ExploreHomeFragment;
import com.ggg.rememo.feature.explore.Fragment.NearbyFragment;
import com.ggg.rememo.feature.explore.Fragment.RecFragment;

import java.util.List;

public class ExplorePageAdapter extends FragmentStateAdapter {
    private List<MemoryPoint> points;
    private List<MemoryPost> posts;

    public ExplorePageAdapter(@NonNull ExploreHomeFragment fragmentActivity) {
        super(fragmentActivity);
    }

    public void setData(List<MemoryPoint> points, List<MemoryPost> posts) {
        this.points = points;
        this.posts = posts;
        // 通知数据变化
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return RecFragment.newInstance(points, posts);
            case 1:
                return NearbyFragment.newInstance(posts);
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
