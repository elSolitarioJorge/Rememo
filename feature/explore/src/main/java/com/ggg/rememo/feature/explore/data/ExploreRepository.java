package com.ggg.rememo.feature.explore.data;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.data.mapper.MemoryPointMapper;
import com.ggg.rememo.core.data.mapper.MemoryPostMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.MemoryPointResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostListItemResponse;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.core.network.NetworkErrorMapper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 发现页数据仓库
 * <p>
 * 负责从网络获取随机记忆列表用于发现页展示。
 * </p>
 */
public class ExploreRepository {

    private static final int DEFAULT_LIMIT = 20;

    private final ApiService apiService;

    public ExploreRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
    }

    /**
     * 获取随机记忆列表（发现页）
     * @param limit 获取数量
     * @param callback 回调
     */
    public void getRandomPosts(int limit, ApiCallback<List<MemoryPost>> callback) {
        apiService.getRandomPosts(limit).enqueue(new Callback<ApiResponse<List<MemoryPostListItemResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<MemoryPostListItemResponse>>> call,
                                   @NonNull Response<ApiResponse<List<MemoryPostListItemResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MemoryPostListItemResponse> responseList = response.body().getData();
                    if (responseList != null && !responseList.isEmpty()) {
                        List<MemoryPost> posts = MemoryPostMapper.fromListItem(responseList);
                        callback.onSuccess(posts);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                } else {
                    callback.onError(mapResponseError(response));
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
     * 获取随机记忆列表（默认数量）
     */
    public void getRandomPosts(ApiCallback<List<MemoryPost>> callback) {
        getRandomPosts(DEFAULT_LIMIT, callback);
    }

    /**
     * 获取所有记忆点（推荐页位置卡片）
     */
    public void getMemoryPoints(ApiCallback<List<MemoryPoint>> callback) {
        apiService.getAllMemoryPoints().enqueue(new Callback<ApiResponse<List<MemoryPointResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<MemoryPointResponse>>> call,
                                   @NonNull Response<ApiResponse<List<MemoryPointResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MemoryPointResponse> responseList = response.body().getData();
                    if (responseList != null && !responseList.isEmpty()) {
                        List<MemoryPoint> points = MemoryPointMapper.toEntityList(responseList);
                        callback.onSuccess(points);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                } else {
                    callback.onError(mapResponseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<MemoryPointResponse>>> call,
                                  @NonNull Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private String mapResponseError(Response<? extends ApiResponse<?>> response) {
        if (response == null) {
            return NetworkErrorMapper.toUserMessage(null);
        }

        ApiResponse<?> body = response.body();
        if (body != null && !body.isSuccess()) {
            return NetworkErrorMapper
                    .fromBusinessCode(body.getCode(), body.getMessage(), null)
                    .getUserMessage();
        }

        if (!response.isSuccessful()) {
            return NetworkErrorMapper
                    .fromHttpCode(response.code(), null, null)
                    .getUserMessage();
        }

        return NetworkErrorMapper
                .fromHttpCode(response.code(), null, "请求失败，请稍后重试")
                .getUserMessage();
    }
}
