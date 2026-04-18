package com.ggg.rememo.feature.timeline.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.feature.timeline.contract.TimelineContract;
import com.ggg.rememo.feature.timeline.data.TimelineRepository;
import com.ggg.rememo.feature.timeline.data.model.TimelineYearModel;

import java.util.List;

/**
 * 时间线模块 Presenter
 * <p>
 * 职责：加载时间线数据、处理用户交互、页面导航
 * </p>
 */
public class TimelinePresenter extends BasePresenter<TimelineContract.View>
        implements TimelineContract.Presenter {

    private final TimelineRepository repository;
    private final Handler mainHandler;
    private String currentPointId;
    private MemoryPoint currentPoint;

    public TimelinePresenter() {
        this.repository = new TimelineRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadTimeline(String pointId) {
        this.currentPointId = pointId;
        loadData(pointId);
    }

    private void loadData(String pointId) {
        repository.getPointById(pointId, new TimelineRepository.Callback<MemoryPoint>() {
            @Override
            public void onSuccess(MemoryPoint result) {
                currentPoint = result;
                mainHandler.post(() -> {
                    if (result != null) {
                        ifViewAttached(view -> view.showHeader(result.getPointName(), result.getLocationAddress()));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showError("加载地点信息失败"));
                });
            }
        });

        repository.getTimelineByPointId(pointId,
                // 首次数据回调（本地或网络）
                new TimelineRepository.Callback<List<TimelineYearModel>>() {
                    @Override
                    public void onSuccess(List<TimelineYearModel> result) {
                        mainHandler.post(() -> {
                            if (result == null || result.isEmpty()) {
                                ifViewAttached(TimelineContract.View::showEmpty);
                            } else {
                                ifViewAttached(view -> view.showTimeline(result));
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        mainHandler.post(() -> {
                            ifViewAttached(view -> {
                                view.showError("加载时间线失败: " + e.getMessage());
                            });
                        });
                    }
                },
                // 网络刷新完成回调
                new TimelineRepository.Callback<List<TimelineYearModel>>() {
                    @Override
                    public void onSuccess(List<TimelineYearModel> result) {
                        mainHandler.post(() -> {
                            if (result != null && !result.isEmpty()) {
                                ifViewAttached(view -> view.refreshTimeline(result));
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // 网络刷新失败静默忽略
                    }
                });
    }

    @Override
    public void onMemoryClicked(String postId) {
        ifViewAttached(view -> view.navigateToMemoryDetail(postId));
    }

    @Override
    public void onAddMemoryClicked() {
        if (currentPoint != null) {
            ifViewAttached(view -> view.navigateToPublish(
                    currentPoint.getPointId(),
                    currentPoint.getLatitude(),
                    currentPoint.getLongitude(),
                    currentPoint.getLocationAddress(),
                    currentPoint.getPointName()));
        }
    }

    @Override
    public void onShareClicked() {
        ifViewAttached(view -> {
            if (currentPoint != null) {
                String subject = currentPoint.getPointName();
                String text = "我在 " + currentPoint.getLocationAddress() +
                        " 有 " + currentPoint.getMemoryCount() + " 个珍贵记忆，" +
                        "一起来记录吧！";
                view.navigateToShare(subject, text);
            }
        });
    }

    @Override
    public void onGatewayClick(TimelineYearModel yearModel) {
        ifViewAttached(view -> view.navigateToExploreYear(yearModel.getYear(), yearModel.getMemoryCount()));
    }

    /**
     * 获取当前地点ID
     */
    public String getCurrentPointId() {
        return currentPointId;
    }
}
