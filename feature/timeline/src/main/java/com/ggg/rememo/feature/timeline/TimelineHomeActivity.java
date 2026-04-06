package com.ggg.rememo.feature.timeline;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.adapter.TimelineYearAdapter;
import com.ggg.rememo.feature.timeline.contract.TimelineContract;
import com.ggg.rememo.feature.timeline.databinding.ActivityTimelineHomeBinding;
import com.ggg.rememo.feature.timeline.model.TimelineYearModel;
import com.ggg.rememo.feature.timeline.presenter.TimelinePresenter;

import java.util.List;

/**
 * 时光隧道 Activity
 */
@Route(path = Routes.Timeline.HOME)
public class TimelineHomeActivity extends AppCompatActivity implements TimelineContract.View {

    private ActivityTimelineHomeBinding binding;
    private TimelinePresenter presenter;
    private TimelineYearAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTimelineHomeBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        initInsets();
        initPresenter();
        initRecyclerView();
        initClickListeners();
        startAnimations();

        // 从 ARouter 参数或 Intent 获取地点ID
        String pointId = getIntent().getStringExtra(Routes.Timeline.EXTRA_POINT_ID);
        presenter.loadTimeline(pointId);
    }

    private void initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });
    }

    private void initPresenter() {
        presenter = new TimelinePresenter();
        presenter.attachView(this);
    }

    private void initRecyclerView() {
        adapter = new TimelineYearAdapter();
        adapter.setOnMemoryClickListener(new TimelineYearAdapter.OnMemoryClickListener() {
            @Override
            public void onMemoryClick(MemoryPost post) {
                presenter.onMemoryClicked(post.getPostId());
            }

            @Override
            public void onGatewayClick(TimelineYearModel yearModel) {
                presenter.onGatewayClick(yearModel);
            }
        });

        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTimeline.setAdapter(adapter);
        binding.rvTimeline.setNestedScrollingEnabled(false);
    }

    private void initClickListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 分享按钮
        binding.btnShare.setOnClickListener(v -> presenter.onShareClicked());

        // 添加记忆 FAB
        binding.fabAdd.setOnClickListener(v -> presenter.onAddMemoryClicked());
    }

    private void startAnimations() {
        View glowView = binding.viewFabGlow;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 0.8f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 0.8f, 1.1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.5f, 1f);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(1500);
        animatorSet.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // ==================== TimelineContract.View 实现 ====================

    @Override
    public void refreshTimeline(List<TimelineYearModel> timelineYears) {
        adapter.refreshYears(timelineYears);
    }

    @Override
    public void showTimeline(List<TimelineYearModel> timelineYears) {
        adapter.setYears(timelineYears);
        binding.rvTimeline.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmpty() {
        Toast.makeText(this, "暂无记忆记录", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showHeader(String title, String subtitle) {
        binding.tvTitle.setText(title);
        binding.tvSubtitle.setText(subtitle);
    }

    @Override
    public void navigateToMemoryDetail(String postId) {
        ARouter.getInstance()
                .build(Routes.Detail.HOME)
                .withString(Routes.Detail.EXTRA_POST_ID, postId)
                .navigation(this);
    }

    /**
     * 跳转到发布页面
     */
    @Override
    public void navigateToPublish(String pointId, double lat, double lng, String address, String pointName) {
        ARouter.getInstance()
                .build(Routes.Publish.HOME)
                .withString(Routes.Publish.EXTRA_POINT_ID, pointId)
                .withDouble(Routes.Publish.EXTRA_LAT, lat)
                .withDouble(Routes.Publish.EXTRA_LNG, lng)
                .withString(Routes.Publish.EXTRA_ADDRESS, address)
                .withString(Routes.Publish.EXTRA_POINT_NAME, pointName)
                .navigation(this);
    }

    @Override
    public void navigateToShare(Intent shareIntent) {
        startActivity(Intent.createChooser(shareIntent, "分享记忆"));
    }

    @Override
    public void showLoading() {
        // TODO: 显示加载状态
    }

    @Override
    public void hideLoading() {
        // TODO: 隐藏加载状态
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToExploreYear(int year, int memoryCount) {
        ARouter.getInstance()
                .build(Routes.Timeline.YEAR_ARCHIVE)
                .withString(Routes.Timeline.EXTRA_POINT_ID, presenter.getCurrentPointId())
                .withInt(Routes.Timeline.EXTRA_YEAR, year)
                .navigation(this);
    }
}
