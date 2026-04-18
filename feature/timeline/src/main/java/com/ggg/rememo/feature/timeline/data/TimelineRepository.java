package com.ggg.rememo.feature.timeline.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.data.mapper.MemoryPostMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.MemoryPostListItemResponse;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.feature.timeline.data.model.TimelineYearModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 时间线模块数据仓库
 * <p>
 * 负责从网络/数据库获取记忆数据，按年份分组转换为 TimelineYearModel。
 * </p>
 */
public class TimelineRepository {

    private final MemoryPostRepository memoryPostRepository;
    private final MemoryPointRepository memoryPointRepository;
    private final ApiService apiService;

    public TimelineRepository() {
        this.memoryPostRepository = new MemoryPostRepository();
        this.memoryPointRepository = new MemoryPointRepository();
        this.apiService = NetworkClient.getInstance().getApiService();
    }

    /**
     * 从网络获取某记忆点的所有记忆
     * @param pointId 地点ID
     * @param callback 回调
     */
    public void fetchPostsByPointId(String pointId, ApiCallback<List<MemoryPost>> callback) {
        apiService.getPostsByPointId(pointId)
                .enqueue(new retrofit2.Callback<ApiResponse<List<MemoryPostListItemResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<MemoryPostListItemResponse>>> call,
                                           @NonNull Response<ApiResponse<List<MemoryPostListItemResponse>>> response) {
                        List<MemoryPostListItemResponse> responseList = null;
                        if (response.body() != null) {
                            responseList = response.body().getData();
                        }
                        if (responseList != null && !responseList.isEmpty()) {
                            List<MemoryPost> posts = MemoryPostMapper.fromListItem(responseList);
                            // 保存到本地数据库
                            memoryPostRepository.insertAll(posts, new MemoryPostRepository.Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(posts);
                                }

                                @Override
                                public void onError(Exception e) {
                                    // 即使保存失败也返回数据
                                    callback.onSuccess(posts);
                                }
                            });
                        } else {
                            callback.onSuccess(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<MemoryPostListItemResponse>>> call,
                                          @NonNull Throwable t) {
                        callback.onFailure(t);
                    }
                });
    }

    /**
     * 按地点ID加载时间线数据（按年份分组，缓存优先 + 网络刷新）
     * <p>
     * 策略：先展示本地缓存保证流畅体验，后台静默拉取网络数据，
     *       网络返回后更新本地 DB 并通过 View 层 DiffUtil 刷新界面。
     * </p>
     *
     * @param pointId     地点ID
     * @param callback    回调（仅在首次本地数据/网络数据到达时触发）
     * @param onRefreshed 网络刷新完成回调（带回最新数据，可用于下拉刷新场景）
     */
    public void getTimelineByPointId(String pointId,
                                     Callback<List<TimelineYearModel>> callback,
                                     Callback<List<TimelineYearModel>> onRefreshed) {
        // 先展示本地缓存
        memoryPostRepository.getByPointId(pointId, new MemoryPostRepository.Callback<List<MemoryPost>>() {
            @Override
            public void onSuccess(List<MemoryPost> localResult) {
                if (localResult != null && !localResult.isEmpty()) {
                    callback.onSuccess(groupByYear(localResult));
                }
                // 后台静默拉取网络数据
                fetchPostsByPointId(pointId, new ApiCallback<List<MemoryPost>>() {
                    @Override
                    public void onSuccess(List<MemoryPost> networkData) {
                        if (networkData != null && !networkData.isEmpty()) {
                            // 网络数据已通过 insertAll 更新到本地 DB
                            // 通过回调通知上层刷新界面（View 层使用 DiffUtil 对比差异）
                            if (onRefreshed != null) {
                                onRefreshed.onSuccess(groupByYear(networkData));
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // 网络失败且本地已有数据：静默忽略，不打扰用户
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // 本地读取失败，直接从网络获取
                fetchPostsByPointId(pointId, new ApiCallback<List<MemoryPost>>() {
                    @Override
                    public void onSuccess(List<MemoryPost> data) {
                        callback.onSuccess(groupByYear(data));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(new Exception(message));
                    }
                });
            }
        });
    }


    /**
     * 获取地点信息
     * @param pointId 地点ID
     * @param callback 回调
     */
    public void getPointById(String pointId, Callback<MemoryPoint> callback) {
        memoryPointRepository.getById(pointId, new MemoryPointRepository.Callback<MemoryPoint>() {
            @Override
            public void onSuccess(MemoryPoint result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private List<TimelineYearModel> groupByYear(List<MemoryPost> posts) {
        Map<Integer, List<MemoryPost>> yearMap = new LinkedHashMap<>();

        for (MemoryPost post : posts) {
            int year = post.getMemoryYear();
            /* computeIfAbsent(year, ...) 会先检查 year 是否存在 Map 中
             * 存在：直接返回关联的 List
             * 不存在：执行后面的Lambda表达式，创建 List 并返回
             */
            yearMap.computeIfAbsent(year, k -> new ArrayList<>()).add(post);
        }

        List<TimelineYearModel> result = new ArrayList<>();
        String[] subtitles = {"流金岁月", "青春记忆", "时光印记", "珍贵时刻", "难忘回忆", "美好时光"};

        int index = 0;
        for (Map.Entry<Integer, List<MemoryPost>> entry : yearMap.entrySet()) {
            String subtitle = subtitles[index % subtitles.length];
            result.add(new TimelineYearModel(entry.getKey(), subtitle, entry.getValue()));
            index++;
        }

        return result;
    }

    /**
     * 通用回调接口
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
