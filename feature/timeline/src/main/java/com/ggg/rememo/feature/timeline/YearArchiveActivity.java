package com.ggg.rememo.feature.timeline;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.adapter.ArchiveAdapter;
import com.ggg.rememo.feature.timeline.contract.ArchiveContract;
import com.ggg.rememo.feature.timeline.databinding.ActivityYearArchiveBinding;
import com.ggg.rememo.feature.timeline.model.SeasonSection;
import com.ggg.rememo.feature.timeline.presenter.ArchivePresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * 年份档案 Activity
 * <p>
 * 展示某一年份的所有记忆，支持"时空热度"（瀑布流）和"季节脉络"（时间轴）两种视图切换。
 */
@Route(path = Routes.Timeline.YEAR_ARCHIVE)
public class YearArchiveActivity extends AppCompatActivity implements ArchiveContract.View {

    private ActivityYearArchiveBinding binding;
    private ArchivePresenter presenter;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityYearArchiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parseIntent();
        initPresenter();
        initViews();
        setupRecyclerView();

        presenter.loadData(currentYear, currentPointId);
    }

    private void parseIntent() {
        currentYear = getIntent().getIntExtra(Routes.Timeline.EXTRA_YEAR, 2026);
        currentPointId = getIntent().getStringExtra(Routes.Timeline.EXTRA_POINT_ID);

        binding.tvYearGlow.setText(String.valueOf(currentYear));
    }

    private void initPresenter() {
        presenter = new ArchivePresenter();
        presenter.attachView(this);
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLocationSubtitle.setSelected(true);

        // 切换监听
        binding.tabHot.setOnClickListener(v -> switchMode(true));
        binding.tabTime.setOnClickListener(v -> switchMode(false));
    }

    private void setupRecyclerView() {
        // 初始化两种管理器
        masonryLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        masonryLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        timelineLayoutManager = new LinearLayoutManager(this);

        // 实例化发光轴线
        spineDecoration = new TimelineSpineDecoration(this);

        // 默认设置为"时空热度"瀑布流
        binding.rvArchive.setLayoutManager(masonryLayoutManager);
        adapter = new ArchiveAdapter();
        binding.rvArchive.setAdapter(adapter);
        adapter.setMode(ArchiveAdapter.MODE_HOT);
        binding.rvArchive.setPadding(dpToPx(4), dpToPx(16), dpToPx(4), dpToPx(100));

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
            binding.tabHot.setBackgroundResource(R.drawable.bg_filter_tab_active);
            binding.tabHot.setTextColor(0xFFF5A623);
            binding.tabTime.setBackground(null);
            binding.tabTime.setTextColor(0xFF64748B);

            // 移除轴线，切换为瀑布流
            binding.rvArchive.removeItemDecoration(spineDecoration);
            binding.rvArchive.setLayoutManager(masonryLayoutManager);
        adapter.setMode(ArchiveAdapter.MODE_HOT);
        binding.rvArchive.setPadding(dpToPx(4), dpToPx(16), dpToPx(4), dpToPx(100));

            // 重新应用瀑布流数据
            adapter.setData(flattenSections(sections));
        } else {
            // UI 状态
            binding.tabTime.setBackgroundResource(R.drawable.bg_filter_tab_active);
            binding.tabTime.setTextColor(0xFFF5A623);
            binding.tabHot.setBackground(null);
            binding.tabHot.setTextColor(0xFF64748B);

            // 添加发光轴线，切换为单列
            binding.rvArchive.addItemDecoration(spineDecoration);
            binding.rvArchive.setLayoutManager(timelineLayoutManager);
            adapter.setMode(ArchiveAdapter.MODE_TIMELINE);
            binding.rvArchive.setPadding(0, dpToPx(16), dpToPx(16), dpToPx(100));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // ==================== ArchiveContract.View 实现 ====================

    @Override
    public void showData(List<SeasonSection> data) {
        this.sections = data;
        binding.rvArchive.setVisibility(View.VISIBLE);
        if (isHotMode) {
            binding.rvArchive.post(() -> adapter.setData(flattenSections(data)));
        } else {
            binding.rvArchive.post(() -> adapter.setTimelineData(data));
        }
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
    public void showPointInfo(MemoryPoint point, int totalCount) {
        // 记忆点名称
        String pointName = point.getPointName();
        if (!TextUtils.isEmpty(pointName)) {
            binding.tvLocationTitle.setText(pointName);
        }
        // 地址副标题
        String address = point.getLocationAddress();
        if (!TextUtils.isEmpty(address)) {
            binding.tvLocationSubtitle.setText(address);
        }
        // 记忆数量
        if (totalCount > 0) {
            binding.tvTotalRecords.setText(totalCount + " 条记忆");
        }
    }
}
