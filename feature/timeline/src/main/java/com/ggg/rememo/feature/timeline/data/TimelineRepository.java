package com.ggg.rememo.feature.timeline.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.data.mapper.MemoryPostMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.MemoryPostListItemResponse;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.feature.timeline.model.TimelineYearModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<MemoryPostListItemResponse> responseList = response.body().getData();
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
                        } else {
                            callback.onError("获取记忆列表失败");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<MemoryPostListItemResponse>>> call,
                                          @NonNull Throwable t) {
                        callback.onError("网络异常: " + t.getMessage());
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
        // Step 1: 先展示本地缓存
        memoryPostRepository.getByPointId(pointId, new MemoryPostRepository.Callback<List<MemoryPost>>() {
            @Override
            public void onSuccess(List<MemoryPost> localResult) {
                if (localResult != null && !localResult.isEmpty()) {
                    callback.onSuccess(groupByYear(localResult));
                }
                // Step 2: 后台静默拉取网络数据
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
                        List<TimelineYearModel> timelineYears = groupByYear(data);
                        callback.onSuccess(timelineYears);
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
     * 兼容旧签名：只返回首次数据，不主动刷新
     *
     * @deprecated 请使用 {@link #getTimelineByPointId(String, Callback, Callback)}
     */
    @Deprecated
    public void getTimelineByPointId(String pointId, Callback<List<TimelineYearModel>> callback) {
        getTimelineByPointId(pointId, callback, null);
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
            if (!yearMap.containsKey(year)) {
                yearMap.put(year, new ArrayList<>());
            }
            Objects.requireNonNull(yearMap.get(year)).add(post);
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
     * 获取演示用时间线数据（不依赖数据库）
     * 用于 UI 效果测试
     */
    public List<TimelineYearModel> getMockTimeline() {
        List<TimelineYearModel> timelineYears = new ArrayList<>();

        // 1998年 - 2个记忆
        List<MemoryPost> posts1998 = new ArrayList<>();
        posts1998.add(createMockPost("1998_1", "绿皮车上的离别", "父亲送我到站台，那是我第一次出远门。", 1998, "冬",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        posts1998.add(createMockPost("1998_2", "站台上的拥抱", "妈妈的叮嘱还在耳边回荡。", 1998, "冬",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        timelineYears.add(new TimelineYearModel(1998, "流金岁月", posts1998));

        // 2008年 - 3个记忆
        List<MemoryPost> posts2008 = new ArrayList<>();
        posts2008.add(createMockPost("2008_1", "高铁开通的第一天", "西安到北京只需要5小时了。", 2008, "夏",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        posts2008.add(createMockPost("2008_2", "站台重逢", "大学放假回家，表哥来接站。", 2008, "秋",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        posts2008.add(createMockPost("2008_3", "奶奶的行李箱", "装满了家乡的特产和思念。", 2008, "春",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        timelineYears.add(new TimelineYearModel(2008, "青春记忆", posts2008));

        // 2015年 - 2个记忆
        List<MemoryPost> posts2015 = new ArrayList<>();
        posts2015.add(createMockPost("2015_1", "送别战友", "服役期满，在站台拥抱告别。", 2015, "秋",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        posts2015.add(createMockPost("2015_2", "第一次带女友回家", "紧张又兴奋的旅程。", 2015, "夏",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        timelineYears.add(new TimelineYearModel(2015, "时光印记", posts2015));

        // 2020年 - 1个记忆
        List<MemoryPost> posts2020 = new ArrayList<>();
        posts2020.add(createMockPost("2020_1", "疫情期间的坚守", "铁路人的责任与担当。", 2020, "春",
                "android.resource://com.ggg.rememo/drawable/sample_image",
                "android.resource://com.ggg.rememo/drawable/sample_avatar"));
        timelineYears.add(new TimelineYearModel(2020, "珍贵时刻", posts2020));

        return timelineYears;
    }

    private MemoryPost createMockPost(String id, String title, String content,
                                       int year, String season, String imageUrl, String avatarUrl) {
        MemoryPost post = new MemoryPost();
        post.setPostId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setMemoryYear(year);
        post.setMemorySeason(season);
        post.setLikeCount((int) (Math.random() * 100));
        post.setCommentCount((int) (Math.random() * 20));
        post.setCollectCount((int) (Math.random() * 50));
        post.setCreatedTime(System.currentTimeMillis());

        List<MemoryPhoto> photos = new ArrayList<>();
        MemoryPhoto photo = new MemoryPhoto(UUID.randomUUID().toString(), imageUrl, null, MemoryPhoto.PhotoState.ORIGINAL);
        photos.add(photo);
        post.setImages(photos);

        return post;
    }

    /**
     * 获取地点信息的模拟数据
     */
    public MemoryPoint getMockPoint() {
        MemoryPoint point = new MemoryPoint();
        point.setPointId("mock_point_1");
        point.setPointName("父亲送我的月台");
        point.setLocationAddress("西安老火车站");
        point.setLatitude(34.3416);
        point.setLongitude(108.9398);
        point.setMemoryCount(156);
        return point;
    }

    /**
     * 通用回调接口
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
