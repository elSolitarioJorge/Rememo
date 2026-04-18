package com.ggg.rememo.feature.explore.presenter;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.feature.explore.data.ExploreRepository;
import com.ggg.rememo.feature.explore.data.NearbyRepository;
import com.ggg.rememo.feature.explore.contract.ExploreContract;

import java.util.ArrayList;
import java.util.List;

/**
 * 发现页 Presenter
 * <p>
 * 职责：加载推荐数据（记忆点 + 帖子）和附近数据（帖子 + Mock 距离）
 * </p>
 */
public class ExplorePresenter extends BasePresenter<ExploreContract.HomeView> implements ExploreContract.Presenter {

    private final ExploreRepository exploreRepository;
    private final NearbyRepository nearbyRepository;

    private ExploreContract.RecView recView;
    private ExploreContract.NearbyView nearbyView;

    public ExplorePresenter() {
        this.exploreRepository = new ExploreRepository();
        this.nearbyRepository = new NearbyRepository();
    }

    public void attachView(ExploreContract.RecView recView, ExploreContract.NearbyView nearbyView) {
        this.recView = recView;
        this.nearbyView = nearbyView;
    }

    public void attachRecView(ExploreContract.RecView recView) {
        this.recView = recView;
    }

    public void attachNearbyView(ExploreContract.NearbyView nearbyView) {
        this.nearbyView = nearbyView;
    }
    public void detachView() {
        this.recView = null;
        this.nearbyView = null;
    }

    public void loadRecData() {
        if (recView != null) {
            recView.showLoading();
        }

        exploreRepository.getMemoryPoints(new ApiCallback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> points) {
                exploreRepository.getRandomPosts(new ApiCallback<List<MemoryPost>>() {
                    @Override
                    public void onSuccess(List<MemoryPost> posts) {
                        if (recView != null) {
                            recView.hideLoading();
                            if ((points == null || points.isEmpty()) && (posts == null || posts.isEmpty())) {
                                recView.showEmpty();
                            } else {
                                recView.showRecData(
                                        points != null ? points : new ArrayList<>(),
                                        posts != null ? posts : new ArrayList<>()
                                );
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (recView != null) {
                            recView.hideLoading();
                            recView.showError(message);
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (recView != null) {
                    recView.hideLoading();
                    recView.showError(message);
                }
            }
        });
    }

    public void loadNearbyData() {
        if (nearbyView != null) {
            nearbyView.showLoading();
        }

        nearbyRepository.getNearbyPosts(new ApiCallback<List<MemoryPost>>() {
            @Override
            public void onSuccess(List<MemoryPost> posts) {
                if (nearbyView != null) {
                    nearbyView.hideLoading();
                    if (posts == null || posts.isEmpty()) {
                        nearbyView.showEmpty();
                    } else {
                        List<String> distances = NearbyRepository.generateMockDistances(posts.size());
                        nearbyView.showNearbyData(posts, distances);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (nearbyView != null) {
                    nearbyView.hideLoading();
                    nearbyView.showError(message);
                }
            }
        });
    }

    public void onRefresh() {
        loadRecData();
        loadNearbyData();
    }
}
