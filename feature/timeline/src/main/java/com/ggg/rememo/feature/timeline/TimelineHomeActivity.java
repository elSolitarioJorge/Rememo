package com.ggg.rememo.feature.timeline;

import android.animation.Animator;
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
import com.ggg.rememo.core.ui.auth.LoginRequiredPrompt;
import com.ggg.rememo.feature.timeline.view.adapter.TimelineYearAdapter;
import com.ggg.rememo.feature.timeline.contract.TimelineContract;
import com.ggg.rememo.feature.timeline.databinding.ActivityTimelineHomeBinding;
import com.ggg.rememo.feature.timeline.data.model.TimelineYearModel;
import com.ggg.rememo.feature.timeline.presenter.TimelinePresenter;

import java.util.ArrayList;
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

    private static final int REQUEST_PUBLISH_MEMORY = 1001;
    private TimelineYearAdapter adapter;
    private AnimatorSet fabGlowAnimatorSet;
    private boolean viewInitialized;

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
        getBinding().tvSubtitle.setSelected(true);
        viewInitialized = true;
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

    private void startFabGlowAnimation() {
        if (fabGlowAnimatorSet != null) {
            return;
        }
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

    private void stopFabGlowAnimation() {
        if (fabGlowAnimatorSet == null) {
            return;
        }

        ArrayList<Animator> animators = fabGlowAnimatorSet.getChildAnimations();
        fabGlowAnimatorSet.cancel();
        fabGlowAnimatorSet.removeAllListeners();
        for (Animator animator : animators) {
            animator.cancel();
            animator.removeAllListeners();
            if (animator instanceof ObjectAnimator) {
                ((ObjectAnimator) animator).setTarget(null);
            }
        }
        fabGlowAnimatorSet = null;

        View glowView = getBinding().viewFabGlow;
        glowView.setScaleX(1f);
        glowView.setScaleY(1f);
        glowView.setAlpha(1f);
        glowView.clearAnimation();
    }

    /**
     * 页面只在真正位于前台且拥有窗口焦点时运行动画。
     * 发布页覆盖时间轴时先停止，返回并完成窗口切换后再恢复。
     */
    private void setPageAnimationsEnabled(boolean enabled) {
        if (!viewInitialized) {
            return;
        }
        if (adapter != null) {
            adapter.setAnimationsEnabled(enabled);
        }
        if (enabled) {
            startFabGlowAnimation();
            getBinding().timeDustView.startDustAnimation();
        } else {
            stopFabGlowAnimation();
            getBinding().timeDustView.stopDustAnimation();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setPageAnimationsEnabled(hasFocus);
    }

    @Override
    protected void onStop() {
        setPageAnimationsEnabled(false);
        super.onStop();
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
        LoginRequiredPrompt.requireLogin(this, "发布记忆", () ->
                ARouter.getInstance()
                        .build(Routes.Publish.HOME)
                        .withString(Routes.Publish.EXTRA_POINT_ID, pointId)
                        .withDouble(Routes.Publish.EXTRA_LAT, lat)
                        .withDouble(Routes.Publish.EXTRA_LNG, lng)
                        .withString(Routes.Publish.EXTRA_ADDRESS, address)
                        .withString(Routes.Publish.EXTRA_POINT_NAME, pointName)
                        .navigation(this, REQUEST_PUBLISH_MEMORY));
    }

    /**
     * 发布成功后重新执行现有的缓存优先加载：Room 中的新帖子会立即展示，
     * 随后的网络回调继续负责校准服务器最新数据。取消发布时不做无意义刷新。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PUBLISH_MEMORY && resultCode == RESULT_OK
                && presenter != null) {
            String pointId = presenter.getCurrentPointId();
            if (pointId != null && !pointId.trim().isEmpty()) {
                presenter.loadTimeline(pointId);
            }
        }
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
        setPageAnimationsEnabled(false);
        viewInitialized = false;

        if (adapter != null) {
            adapter.release();
            getBinding().rvTimeline.setAdapter(null);
            adapter = null;
        }

        getBinding().timeDustView.releaseDustAnimation();
        getBinding().tvSubtitle.setSelected(false);
        getBinding().btnBack.setOnClickListener(null);
        getBinding().btnShare.setOnClickListener(null);
        getBinding().fabAdd.setOnClickListener(null);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), null);

        super.onDestroy();
    }
}
