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
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.Adapter.ExplorePageAdapter;
import com.ggg.rememo.feature.explore.R;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

@Route(path = Routes.Explore.HOME_FRAGMENT)
public class ExploreHomeFragment extends Fragment {
    private FragmentExploreHomeBinding binding;

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

        // 添加数据
        List<MemoryPoint> points = new ArrayList<>();
        List<MemoryPost> posts = new ArrayList<>();

        // 锚点列表
        points.add(new MemoryPoint("西安老火车站", null, 342));
        points.add(new MemoryPoint("钟楼邮局老信箱", null, 128));
        points.add(new MemoryPoint("钟楼", null, 122));

        // 帖子列表
        posts.add(new MemoryPost("弥巷", "绿皮车上的离别，和那袋橘子", "绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子", 1998, 119));
        posts.add(new MemoryPost("弥巷", "绿皮车上的离别，和那袋橘子", "绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子", 1998, 119));
        posts.add(new MemoryPost("弥巷", "绿皮车上的离别，和那袋橘子", "绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子绿皮车上的离别，和那袋橘子", 1998, 119));


        ExplorePageAdapter adapter = new ExplorePageAdapter(this);
        adapter.setData(points, posts);

        // 设置ViewPager2适配器
        binding.exploreHomeViewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.exploreHomeViewPager2.setAdapter(adapter);


        // 绑定TabLayout
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
