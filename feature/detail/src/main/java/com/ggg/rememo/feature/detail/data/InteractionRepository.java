package com.ggg.rememo.feature.detail.data;

import com.ggg.rememo.core.data.model.network.response.CollectResponse;
import com.ggg.rememo.core.data.model.network.response.LikeResponse;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;

/**
 * 互动操作 Repository（点赞 / 收藏）。
 * 网络调用 + 本地数据同步，统一在 feature 层处理，不污染 core:data。
 */
public class InteractionRepository {

    private final ApiService apiService;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public InteractionRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
    }

    /**
     * 切换点赞状态。
     */
    public void toggleLike(String postId, Callback<LikeResponse> callback) {
        apiService.toggleLike(postId).enqueue(new retrofit2.Callback<ApiResponse<LikeResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<LikeResponse>> call,
                                   retrofit2.Response<ApiResponse<LikeResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LikeResponse likeResponse = response.body().getData();
                    if (callback != null) {
                        callback.onSuccess(likeResponse);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(response.body() != null ? response.body().getMessage() : "点赞失败");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<LikeResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.onError(t.getMessage());
                }
            }
        });
    }

    /**
     * 切换收藏状态。
     */
    public void toggleCollect(String postId, Callback<CollectResponse> callback) {
        apiService.toggleCollect(postId).enqueue(new retrofit2.Callback<ApiResponse<CollectResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<CollectResponse>> call,
                                   retrofit2.Response<ApiResponse<CollectResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CollectResponse collectResponse = response.body().getData();
                    if (callback != null) {
                        callback.onSuccess(collectResponse);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(response.body() != null ? response.body().getMessage() : "收藏失败");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<CollectResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.onError(t.getMessage());
                }
            }
        });
    }
}
