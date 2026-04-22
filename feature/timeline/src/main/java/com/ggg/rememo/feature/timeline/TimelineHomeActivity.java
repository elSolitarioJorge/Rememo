package com.ggg.rememo.feature.timeline;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.base.BaseActivity;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.view.adapter.TimelineYearAdapter;
import com.ggg.rememo.feature.timeline.contract.TimelineContract;
import com.ggg.rememo.feature.timeline.databinding.ActivityTimelineHomeBinding;
import com.ggg.rememo.feature.timeline.data.model.TimelineYearModel;
import com.ggg.rememo.feature.timeline.presenter.TimelinePresenter;

import java.util.List;

/**
 * 时光隧道 Activity
 */
@Route(path = Routes.Timeline.HOME)
public class TimelineHomeActivity extends BaseActivity<
        ActivityTimelineHomeBinding,
        TimelineContract.View,
        TimelinePresenter>
        implements TimelineContract.View {

    private TimelineYearAdapter adapter;
    private AnimatorSet fabGlowAnimatorSet;

    @Override
    protected ActivityTimelineHomeBinding inflateBinding(@NonNull LayoutInflater inflater) {
        return ActivityTimelineHomeBinding.inflate(inflater);
    }

    @Override
    protected TimelinePresenter createPresenter() {
        return new TimelinePresenter();
    }

    @Override
    protected TimelineContract.View getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        initRecyclerView();
        initClickListeners();
        startAnimations();
        getBinding().tvSubtitle.setSelected(true);
    }

    @Override
    protected void initData() {
        String pointId = getIntent().getStringExtra(Routes.Timeline.EXTRA_POINT_ID);
        presenter.loadTimeline(pointId);
    }

    @Override
    protected void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            getBinding().headerLayout.setPadding(
                    getBinding().headerLayout.getPaddingLeft(),
                    systemBars.top,
                    getBinding().headerLayout.getPaddingRight(),
                    getBinding().headerLayout.getPaddingBottom());
            return insets;
        });
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

        getBinding().rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvTimeline.setAdapter(adapter);
        getBinding().rvTimeline.setNestedScrollingEnabled(false);
    }

    private void initClickListeners() {
        // 返回按钮
        getBinding().btnBack.setOnClickListener(v -> finish());

        // 分享按钮
        getBinding().btnShare.setOnClickListener(v -> presenter.onShareClicked());

        // 添加记忆 FAB
        getBinding().fabAdd.setOnClickListener(v -> presenter.onAddMemoryClicked());
    }

    private void startAnimations() {
        View glowView = getBinding().viewFabGlow;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(glowView, "scaleX", 0.8f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(glowView, "scaleY", 0.8f, 1.1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(glowView, "alpha", 0.5f, 1f);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);

        fabGlowAnimatorSet = new AnimatorSet();
        fabGlowAnimatorSet.playTogether(scaleX, scaleY, alpha);
        fabGlowAnimatorSet.setDuration(1500);
        fabGlowAnimatorSet.start();
    }

    // ==================== TimelineContract.View 实现 ====================

    @Override
    public void refreshTimeline(List<TimelineYearModel> timelineYears) {
        adapter.refreshYears(timelineYears);
    }

    @Override
    public void showTimeline(List<TimelineYearModel> timelineYears) {
        adapter.setYears(timelineYears);
        getBinding().rvTimeline.setVisibility(View.VISIBLE);
    }

    @Override
    public void showHeader(String title, String subtitle) {
        getBinding().tvTitle.setText(title);
        getBinding().tvSubtitle.setText(subtitle);
    }

    @Override
    public void showEmpty() {
        Toast.makeText(this, "暂无记忆记录", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** 跳转到帖子详情页面 */
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

    /** 跳转分享 */
    @Override
    public void navigateToShare(String subject, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "分享记忆"));
    }

    /** 跳转年份探索页 */
    @Override
    public void navigateToExploreYear(int year, int memoryCount) {
        ARouter.getInstance()
                .build(Routes.Timeline.YEAR_ARCHIVE)
                .withString(Routes.Timeline.EXTRA_POINT_ID, presenter.getCurrentPointId())
                .withInt(Routes.Timeline.EXTRA_YEAR, year)
                .navigation(this);
    }

    @Override
    protected void onDestroy() {
        if (fabGlowAnimatorSet != null) {
            fabGlowAnimatorSet.cancel();
            fabGlowAnimatorSet = null;
        }
        super.onDestroy();
    }
}
