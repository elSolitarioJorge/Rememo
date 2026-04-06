package com.ggg.rememo.feature.explore.data;

import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.network.ApiCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 附近页数据仓库（演示用）
 * <p>
 * 调用 /api/posts/random 获取帖子数据，Mock 距离列表用于演示。
 * </p>
 */
public class NearbyRepository {

    private static final int DEFAULT_LIMIT = 20;

    private final ExploreRepository exploreRepository;

    public NearbyRepository() {
        this.exploreRepository = new ExploreRepository();
    }

    /**
     * 获取附近帖子（演示用）
     * @param callback 帖子回调 + Mock 距离列表
     */
    public void getNearbyPosts(ApiCallback<List<MemoryPost>> callback) {
        exploreRepository.getRandomPosts(DEFAULT_LIMIT, new ApiCallback<List<MemoryPost>>() {
            @Override
            public void onSuccess(List<MemoryPost> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /**
     * 生成 Mock 距离列表（演示用）
     */
    public static List<String> generateMockDistances(int size) {
        List<String> distances = new ArrayList<>();
        String[] mockValues = {"150m", "320m", "500m", "800m", "1.2km", "1.5km", "2km", "3km"};
        for (int i = 0; i < size; i++) {
            distances.add(mockValues[i % mockValues.length]);
        }
        return distances;
    }
}
