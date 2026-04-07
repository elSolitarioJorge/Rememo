package com.ggg.rememo.feature.detail.data;

import com.ggg.rememo.core.data.mapper.MemoryPostMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.MemoryPostDetailResponse;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;

/**
 * 记忆详情数据 Repository（网络优先，失败降级到本地）。
 *
 * <p>loadMemory 策略：
 * <ol>
 *   <li>先从本地 Room 查询（快速响应）</li>
 *   <li>本地存在则直接返回，同时后台发起网络请求刷新数据</li>
 *   <li>本地不存在则直接发起网络请求</li>
 *   <li>网络成功：写入本地缓存 → 回调给 Presenter</li>
 *   <li>网络失败：降级到本地查询 → 回调给 Presenter</li>
 * </ol>
 */
public class MemoryDetailRepository {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final ApiService apiService;
    private final MemoryPostRepository localRepository;

    public MemoryDetailRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
        this.localRepository = new MemoryPostRepository();
    }

    /**
     * 加载记忆详情，网络优先。
     *
     * @param postId  记忆 ID
     * @param callback 回调
     */
    public void loadMemory(String postId, Callback<MemoryPost> callback) {
        if (postId == null || postId.isEmpty()) {
            if (callback != null) {
                callback.onError("记忆不存在");
            }
            return;
        }

        // 先查本地
        localRepository.getById(postId, new MemoryPostRepository.Callback<MemoryPost>() {
            @Override
            public void onSuccess(MemoryPost localResult) {
                // 本地有数据 → 先展示，后台刷新
                if (localResult != null) {
                    if (callback != null) {
                        callback.onSuccess(localResult);
                    }
                    // 后台静默刷新数据（不覆盖当前 UI）
                    fetchFromNetwork(postId, null);
                } else {
                    // 本地无数据 → 直接走网络
                    fetchFromNetwork(postId, callback);
                }
            }

            @Override
            public void onError(Exception e) {
                // 本地查询失败 → 直接走网络
                fetchFromNetwork(postId, callback);
            }
        });
    }

    /**
     * 从网络获取记忆详情，成功后写入本地。
     *
     * @param postId  记忆 ID
     * @param callback 回调（可能为 null，用于后台静默刷新场景）
     */
    private void fetchFromNetwork(String postId, Callback<MemoryPost> callback) {
        apiService.getPostDetail(postId).enqueue(new retrofit2.Callback<ApiResponse<MemoryPostDetailResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<MemoryPostDetailResponse>> call,
                                   retrofit2.Response<ApiResponse<MemoryPostDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    MemoryPostDetailResponse data = response.body().getData();
                    MemoryPost post = MemoryPostMapper.fromDetail(data);
                    if (post != null) {
                        // 写入本地缓存
                        localRepository.insert(post, null);
                        // 回调给 Presenter（如果有）
                        if (callback != null) {
                            callback.onSuccess(post);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("数据解析失败");
                        }
                    }
                } else {
                    // 网络返回业务错误，降级到本地
                    loadFromLocal(postId, callback);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<MemoryPostDetailResponse>> call, Throwable t) {
                // 网络请求失败，降级到本地
                loadFromLocal(postId, callback);
            }
        });
    }

    /**
     * 从本地数据库读取。
     */
    private void loadFromLocal(String postId, Callback<MemoryPost> callback) {
        localRepository.getById(postId, new MemoryPostRepository.Callback<MemoryPost>() {
            @Override
            public void onSuccess(MemoryPost result) {
                if (callback != null) {
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("记忆不存在");
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "加载失败");
                }
            }
        });
    }
}