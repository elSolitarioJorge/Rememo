package com.ggg.rememo.feature.timeline.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.feature.timeline.contract.ArchiveContract;
import com.ggg.rememo.feature.timeline.data.TimelineRepository;
import com.ggg.rememo.feature.timeline.data.model.SeasonSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 年份档案模块 Presenter
 * <p>
 * 职责：加载年份记忆数据。通过 TimelineRepository 获取真实网络数据，
 * 在内存中按季节分组（冬秋夏春），为空时回调错误信息。
 * </p>
 */
public class ArchivePresenter extends BasePresenter<ArchiveContract.View>
        implements ArchiveContract.Presenter {

    private final TimelineRepository repository;
    private String currentPointId;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ArchivePresenter() {
        this.repository = new TimelineRepository();
    }

    @Override
    public void loadData(int year, String pointId) {
        this.currentPointId = pointId;
        repository.fetchPostsByPointId(pointId, new ApiCallback<List<MemoryPost>>() {
            @Override
            public void onSuccess(List<MemoryPost> allPosts) {

                List<MemoryPost> filteredPosts = filterByYear(allPosts, year);
                List<SeasonSection> sections = groupBySeason(filteredPosts);
                mainHandler.post(() ->
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showData(sections);
                    })
                );
                // 加载记忆点信息（与列表数据并行请求）
                loadPointInfo(filteredPosts.size());
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() ->
                    ifViewAttached(view -> {
                        view.hideLoading();
                        view.showError(message);
                    })
                );
                // 网络失败时仍尝试加载点信息
                loadPointInfo(0);
            }
        });
    }

    private void loadPointInfo(int totalCount) {
        repository.getPointById(currentPointId, new TimelineRepository.Callback<MemoryPoint>() {
            @Override
            public void onSuccess(MemoryPoint point) {
                ifViewAttached(view -> view.showPointInfo(point, totalCount));
            }

            @Override
            public void onError(Exception e) {
                // 加载失败时构造一个兜底数据，不阻塞主流程
                MemoryPoint fallback = new MemoryPoint();
                fallback.setPointId(currentPointId);
                ifViewAttached(view -> view.showPointInfo(fallback, totalCount));
            }
        });
    }

    private List<MemoryPost> filterByYear(List<MemoryPost> posts, int year) {
        List<MemoryPost> result = new ArrayList<>();
        if (posts == null) return result;
        for (MemoryPost post : posts) {
            if (post.getMemoryYear() == year) {
                result.add(post);
            }
        }
        return result;
    }

    private List<SeasonSection> groupBySeason(List<MemoryPost> posts) {
        // 按季节分组，保持插入顺序
        Map<String, List<MemoryPost>> seasonMap = new LinkedHashMap<>();
        for (String season : SeasonSection.SEASON_ORDER) {
            seasonMap.put(season, new ArrayList<>());
        }
        for (MemoryPost post : posts) {
            String season = post.getMemorySeason();
            if ("春".equals(season)) {
                season = SeasonSection.SEASON_SPRING;
            } else if ("夏".equals(season)) {
                season = SeasonSection.SEASON_SUMMER;
            } else if ("秋".equals(season)) {
                season = SeasonSection.SEASON_AUTUMN;
            } else {
                season = SeasonSection.SEASON_WINTER;
            }
            List<MemoryPost> list = seasonMap.get(season);
            if (list != null) {
                list.add(post);
            } else {
                seasonMap.get(SeasonSection.SEASON_WINTER).add(post);
            }
        }

        // 按固定顺序构建 SeasonSection，只保留有记忆的季节
        List<SeasonSection> sections = new ArrayList<>();
        for (String season : SeasonSection.SEASON_ORDER) {
            List<MemoryPost> seasonPosts = seasonMap.get(season);
            if (seasonPosts != null && !seasonPosts.isEmpty()) {
                sections.add(new SeasonSection(season, seasonPosts));
            }
        }
        return sections;
    }
}
