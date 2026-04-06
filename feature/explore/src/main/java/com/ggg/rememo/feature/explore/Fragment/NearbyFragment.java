package com.ggg.rememo.feature.explore.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.Adapter.NearbyPostAdapter;
import com.ggg.rememo.feature.explore.contract.ExploreContract;
import com.ggg.rememo.feature.explore.databinding.FragmentExploreNearbyBinding;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment implements ExploreContract.NearbyView {

    private FragmentExploreNearbyBinding binding;
    private NearbyPostAdapter adapter;
    private ExploreContract.Presenter presenter;
    private boolean hasLoadedOnce = false;

    public interface OnParentAttach {
        void onNearbyViewAttached(NearbyFragment fragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreNearbyBinding.inflate(inflater, container, false);

        initRecyclerView();
        initRefreshClick();

        if (getParentFragment() instanceof OnParentAttach) {
            ((OnParentAttach) getParentFragment()).onNearbyViewAttached(this);
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null && !hasLoadedOnce) {
            hasLoadedOnce = true;
            presenter.loadNearbyData();
        }
    }

    private void initRecyclerView() {
        binding.exploreNearbyContentRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new NearbyPostAdapter(new ArrayList<>(), new ArrayList<>());
        adapter.setOnItemClickListener(post -> {
            ARouter.getInstance()
                    .build(Routes.Detail.HOME)
                    .withString(Routes.Detail.EXTRA_POST_ID, post.getPostId())
                    .navigation();
        });
        binding.exploreNearbyContentRecyclerView.setAdapter(adapter);
    }

    private void initRefreshClick() {
        binding.iconRefresh.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.loadNearbyData();
            }
        });
        binding.textRefresh.setOnClickListener(v -> {
            if (presenter != null) {
                presenter.loadNearbyData();
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
        binding.exploreNearbyContentRecyclerView.setVisibility(View.GONE);
        binding.layoutEmpty.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        if (binding == null) return;
        binding.layoutLoading.getRoot().setVisibility(View.GONE);
        binding.exploreNearbyContentRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showNearbyData(List<MemoryPost> posts, List<String> distances) {
        if (binding == null) return;
        binding.layoutEmpty.getRoot().setVisibility(View.GONE);
        binding.exploreNearbyContentRecyclerView.setVisibility(View.VISIBLE);
        adapter.updateData(posts, distances);
    }

    @Override
    public void showEmpty() {
        if (binding == null) return;
        binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
        binding.exploreNearbyContentRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void setRefreshing(boolean refreshing) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
