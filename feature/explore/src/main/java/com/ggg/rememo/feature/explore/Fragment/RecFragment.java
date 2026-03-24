package com.ggg.rememo.feature.explore.Fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.Utils.UIUtils;
import com.ggg.rememo.feature.explore.Adapter.RecLocAdapter;
import com.ggg.rememo.feature.explore.Adapter.RecPostAdapter;
import com.ggg.rememo.feature.explore.Utils.HorizontalItemDecoration;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreRecommendationBinding;

import java.util.ArrayList;
import java.util.List;


public class RecFragment extends Fragment {
    private static final String ARG_POINTS = "points";
    private static final String ARG_POSTS = "posts";
    public static RecFragment newInstance(List<MemoryPoint> points, List<MemoryPost> posts) {
        RecFragment fragment = new RecFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_POINTS, (ArrayList<? extends Parcelable>) points);
        args.putParcelableArrayList(ARG_POSTS, (ArrayList<? extends Parcelable>) posts);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentExploreRecommendationBinding binding = FragmentExploreRecommendationBinding.inflate(inflater, container, false);
        // 初始化 RecyclerView

        binding.exploreRecoLocationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.exploreRecoLocationRecyclerView.addItemDecoration(new HorizontalItemDecoration(UIUtils.dpToPx(getContext(), 4)));
        binding.exploreRecoContentRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // 获取参数
        List<MemoryPoint> points = getArguments() != null ? getArguments().getParcelableArrayList(ARG_POINTS) : null;
        List<MemoryPost> posts = getArguments() != null ? getArguments().getParcelableArrayList(ARG_POSTS) : null;

        // 设置适配器
        binding.exploreRecoLocationRecyclerView.setAdapter(new RecLocAdapter(points));
        binding.exploreRecoContentRecyclerView.setAdapter(new RecPostAdapter(posts));

        return binding.getRoot();
    }
}
