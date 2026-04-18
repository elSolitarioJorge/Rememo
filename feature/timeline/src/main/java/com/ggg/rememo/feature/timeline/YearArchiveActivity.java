package com.ggg.rememo.feature.timeline;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.base.BaseActivity;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.view.TimelineSpineDecoration;
import com.ggg.rememo.feature.timeline.view.adapter.ArchiveAdapter;
import com.ggg.rememo.feature.timeline.contract.ArchiveContract;
import com.ggg.rememo.feature.timeline.databinding.ActivityYearArchiveBinding;
import com.ggg.rememo.feature.timeline.data.model.SeasonSection;
import com.ggg.rememo.feature.timeline.presenter.ArchivePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * 年份档案 Activity
 * <p>
 * 展示某一年份的所有记忆，支持"时空热度"（瀑布流）和"季节脉络"（时间轴）两种视图切换。
 */
@Route(path = Routes.Timeline.YEAR_ARCHIVE)
public class YearArchiveActivity extends BaseActivity<
        ActivityYearArchiveBinding,
        ArchiveContract.View,
        ArchivePresenter>
        implements ArchiveContract.View {
    private ArchiveAdapter adapter;

    // 布局管理器
    private StaggeredGridLayoutManager masonryLayoutManager;
    private LinearLayoutManager timelineLayoutManager;

    // 自定义发光轴线绘制器
    private TimelineSpineDecoration spineDecoration;

    private int currentYear;
    private String currentPointId;

    // 季节分组数据，供两种模式复用
    private List<SeasonSection> sections = new ArrayList<>();

    // 当前是否为瀑布流模式
    private boolean isHotMode = true;

    @Override
    protected ActivityYearArchiveBinding inflateBinding(@NonNull LayoutInflater inflater) {
        return ActivityYearArchiveBinding.inflate(inflater);
    }
    @Override
    protected ArchivePresenter createPresenter() {
        return new ArchivePresenter();
    }
    @Override
    protected ArchiveContract.View getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        initListeners();
        initRecyclerView();
        getBinding().tvLocationSubtitle.setSelected(true);
    }

    @Override
    protected void initData() {
        parseIntent();
        presenter.loadData(currentYear, currentPointId);
    }

    private void parseIntent() {
        currentYear = getIntent().getIntExtra(Routes.Timeline.EXTRA_YEAR, 2026);
        currentPointId = getIntent().getStringExtra(Routes.Timeline.EXTRA_POINT_ID);
        getBinding().tvYearGlow.setText(String.valueOf(currentYear));
    }

    private void initListeners() {
        getBinding().btnBack.setOnClickListener(v -> finish());
        getBinding().tabHot.setOnClickListener(v -> switchMode(true));
        getBinding().tabTime.setOnClickListener(v -> switchMode(false));
    }

    private void initRecyclerView() {
        // 初始化两种管理器
        masonryLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        masonryLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        timelineLayoutManager = new LinearLayoutManager(this);

        // 实例化发光轴线
        spineDecoration = new TimelineSpineDecoration(this);

        // 默认设置为"时空热度"瀑布流
        getBinding().rvArchive.setLayoutManager(masonryLayoutManager);
        adapter = new ArchiveAdapter();
        getBinding().rvArchive.setAdapter(adapter);
        adapter.setMode(ArchiveAdapter.MODE_HOT);
        getBinding().rvArchive.setPadding(dpToPx(4), dpToPx(16), dpToPx(4), dpToPx(100));

        // Item 点击跳转到详情页
        adapter.setOnItemClickListener(post -> {
            ARouter.getInstance()
                    .build(Routes.Detail.HOME)
                    .withString(Routes.Detail.EXTRA_POST_ID, post.getPostId())
                    .navigation(this);
        });
    }

    /**
     * 核心逻辑：一键切换双列瀑布流与单列时间轴
     * @param isHotMode true: 时空热度（瀑布流），false: 季节脉络（单列）
     */
    private void switchMode(boolean isHotMode) {
        this.isHotMode = isHotMode;

        if (isHotMode) {
            // UI 状态
            getBinding().tabHot.setBackgroundResource(R.drawable.bg_filter_tab_active);
            getBinding().tabHot.setTextColor(0xFFF5A623);
            getBinding().tabTime.setBackground(null);
            getBinding().tabTime.setTextColor(0xFF64748B);

            // 移除轴线，切换为瀑布流
            getBinding().rvArchive.removeItemDecoration(spineDecoration);
            getBinding().rvArchive.setLayoutManager(masonryLayoutManager);
        adapter.setMode(ArchiveAdapter.MODE_HOT);
        getBinding().rvArchive.setPadding(dpToPx(4), dpToPx(16), dpToPx(4), dpToPx(100));

            // 重新应用瀑布流数据
            adapter.setData(flattenSections(sections));
        } else {
            // UI 状态
            getBinding().tabTime.setBackgroundResource(R.drawable.bg_filter_tab_active);
            getBinding().tabTime.setTextColor(0xFFF5A623);
            getBinding().tabHot.setBackground(null);
            getBinding().tabHot.setTextColor(0xFF64748B);

            // 添加发光轴线，切换为单列
            getBinding().rvArchive.addItemDecoration(spineDecoration);
            getBinding().rvArchive.setLayoutManager(timelineLayoutManager);
            adapter.setMode(ArchiveAdapter.MODE_TIMELINE);
            getBinding().rvArchive.setPadding(0, dpToPx(16), dpToPx(16), dpToPx(100));

            // 重新应用季节脉络数据
            adapter.setTimelineData(sections);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private List<MemoryPost> flattenSections(List<SeasonSection> sections) {
        List<MemoryPost> flatPosts = new ArrayList<>();
        for (SeasonSection section : sections) {
            flatPosts.addAll(section.posts);
        }
        return flatPosts;
    }

    // ==================== ArchiveContract.View 实现 ====================

    @Override
    public void showData(List<SeasonSection> data) {
        this.sections = data;
        getBinding().rvArchive.setVisibility(View.VISIBLE);
        if (isHotMode) {
            getBinding().rvArchive.post(() -> adapter.setData(flattenSections(data)));
        } else {
            getBinding().rvArchive.post(() -> adapter.setTimelineData(data));
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPointInfo(MemoryPoint point, int totalCount) {
        // 记忆点名称
        String pointName = point.getPointName();
        if (!TextUtils.isEmpty(pointName)) {
            getBinding().tvLocationTitle.setText(pointName);
        }
        // 地址副标题
        String address = point.getLocationAddress();
        if (!TextUtils.isEmpty(address)) {
            getBinding().tvLocationSubtitle.setText(address);
        }
        // 记忆数量
        if (totalCount > 0) {
            getBinding().tvTotalRecords.setText(totalCount + " 条记忆");
        }
    }
}
