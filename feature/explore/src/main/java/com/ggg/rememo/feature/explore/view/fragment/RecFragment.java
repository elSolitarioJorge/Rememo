package com.ggg.rememo.feature.explore.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.Utils.UIUtils;
import com.ggg.rememo.feature.explore.view.adapter.RecLocAdapter;
import com.ggg.rememo.feature.explore.view.adapter.RecPostAdapter;
import com.ggg.rememo.feature.explore.view.util.HorizontalItemDecoration;
import com.ggg.rememo.feature.explore.contract.ExploreContract;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreRecommendationBinding;

import java.util.ArrayList;
import java.util.List;

public class RecFragment extends Fragment implements ExploreContract.RecView {

    private FragmentExploreRecommendationBinding binding;
    private RecLocAdapter locAdapter;
    private RecPostAdapter postAdapter;
    private ExploreContract.Presenter presenter;
    private boolean hasLoadedOnce = false;

    public interface OnParentAttach {
        void onRecViewAttached(RecFragment fragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreRecommendationBinding.inflate(inflater, container, false);

        initRecyclerViews();

        if (getParentFragment() instanceof OnParentAttach) {
            ((OnParentAttach) getParentFragment()).onRecViewAttached(this);
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null && !hasLoadedOnce) {
            hasLoadedOnce = true;
            presenter.loadRecData();
        }
    }

    private void initRecyclerViews() {
        binding.exploreRecoLocationRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.exploreRecoLocationRecyclerView.addItemDecoration(
                new HorizontalItemDecoration(UIUtils.dpToPx(getContext(), 4)));

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        binding.exploreRecoContentRecyclerView.setLayoutManager(layoutManager);

        if (binding.exploreRecoContentRecyclerView.getItemAnimator() != null) {
            binding.exploreRecoContentRecyclerView.getItemAnimator().setChangeDuration(0);
        }

        locAdapter = new RecLocAdapter();
        postAdapter = new RecPostAdapter(new ArrayList<>());

        postAdapter.setOnItemClickListener(post -> {
            ARouter.getInstance()
                    .build(Routes.Detail.HOME)
                    .withString(Routes.Detail.EXTRA_POST_ID, post.getPostId())
                    .navigation();
        });

        binding.exploreRecoLocationRecyclerView.setAdapter(locAdapter);
        binding.exploreRecoContentRecyclerView.setAdapter(postAdapter);

        binding.exploreRecoContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    layoutManager.invalidateSpanAssignments();
                }
            }
        });
    }

    public void setPresenter(ExploreContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLoading() {
        if (binding == null) return;
        binding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.exploreRecoLocationRecyclerView.setVisibility(View.GONE);
        binding.exploreRecoContentRecyclerView.setVisibility(View.GONE);
        binding.layoutEmpty.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        if (binding == null) return;
        binding.layoutLoading.getRoot().setVisibility(View.GONE);
        binding.exploreRecoLocationRecyclerView.setVisibility(View.VISIBLE);
        binding.exploreRecoContentRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showRecData(List<MemoryPoint> points, List<MemoryPost> posts) {
        if (binding == null) return;
        binding.layoutEmpty.getRoot().setVisibility(View.GONE);
        binding.exploreRecoLocationRecyclerView.setVisibility(View.VISIBLE);
        binding.exploreRecoContentRecyclerView.setVisibility(View.VISIBLE);

        locAdapter.updateData(points);
        postAdapter.updateData(posts);
    }

    @Override
    public void showEmpty() {
        if (binding == null) return;
        binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
        binding.exploreRecoLocationRecyclerView.setVisibility(View.GONE);
        binding.exploreRecoContentRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
