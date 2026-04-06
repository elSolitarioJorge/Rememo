package com.ggg.rememo.feature.explore.contract;

import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;

/**
 * 发现页（Explore）模块 MVP 契约接口
 */
public interface ExploreContract {

    /**
     * 推荐标签页 View 接口
     */
    interface RecView extends BaseView {
        void showRecData(List<MemoryPoint> points, List<MemoryPost> posts);
        void showEmpty();
        void setRefreshing(boolean refreshing);
    }

    /**
     * 附近标签页 View 接口
     */
    interface NearbyView extends BaseView {
        void showNearbyData(List<MemoryPost> posts, List<String> distances);
        void showEmpty();
        void setRefreshing(boolean refreshing);
    }

    /**
     * Presenter 接口
     */
    interface Presenter {
        void attachView(RecView recView, NearbyView nearbyView);
        void detachView();
        void loadRecData();
        void loadNearbyData();
        void onRefresh();
    }
}
