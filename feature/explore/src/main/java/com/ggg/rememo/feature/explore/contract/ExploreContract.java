package com.ggg.rememo.feature.explore.contract;

import com.ggg.rememo.core.base.IBaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;

/**
 * 发现页（Explore）模块 MVP 契约接口
 */
public interface ExploreContract {

    interface HomeView extends IBaseView {

    }

    /**
     * 推荐标签页 View 接口
     */
    interface RecView extends IBaseView {
        void showRecData(List<MemoryPoint> points, List<MemoryPost> posts);
        void showEmpty();
    }

    /**
     * 附近标签页 View 接口
     */
    interface NearbyView extends IBaseView {
        void showNearbyData(List<MemoryPost> posts, List<String> distances);
        void showEmpty();
    }

    /**
     * Presenter 接口
     */
    interface Presenter {
        void loadRecData();
        void loadNearbyData();
    }
}
