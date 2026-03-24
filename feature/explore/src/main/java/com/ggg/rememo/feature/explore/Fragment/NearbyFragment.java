package com.ggg.rememo.feature.explore.Fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.Adapter.NearbyPostAdapter;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreNearbyBinding;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {
    private static final String ARG_POSTS = "posts";
    public static NearbyFragment newInstance(List<MemoryPost> posts) {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_POSTS, (ArrayList<? extends Parcelable>) posts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentExploreNearbyBinding binding = FragmentExploreNearbyBinding.inflate(inflater, container, false);

        // 初始化RecyclerView
        binding.exploreNearbyContentRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // 获取参数
        List<MemoryPost> posts = getArguments() != null ? getArguments().getParcelableArrayList(ARG_POSTS) : null;

        // 设置适配器
        binding.exploreNearbyContentRecyclerView.setAdapter(new NearbyPostAdapter(posts));

        return binding.getRoot();
    }
}
