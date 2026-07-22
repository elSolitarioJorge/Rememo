package com.ggg.rememo.feature.detail.data;

import com.ggg.rememo.core.data.mapper.CommentMapper;
import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.core.data.model.network.request.CreateCommentRequest;
import com.ggg.rememo.core.data.model.network.response.CommentResponse;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.core.network.NetworkErrorMapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 评论数据 Repository（网络 + 本地缓存）。
 */
public class CommentRepository {

    private final ApiService apiService;
    private final com.ggg.rememo.core.data.repository.CommentRepository localRepository;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public CommentRepository() {
        this.apiService = NetworkClient.getInstance().getApiService();
        this.localRepository = new com.ggg.rememo.core.data.repository.CommentRepository();
    }

    /**
     * 加载评论列表（网络优先，失败则降级到本地）。
     */
    public void loadComments(String postId, int page, int size, Callback<List<Comment>> callback) {
        apiService.getCommentsByPostId(postId, page, size).enqueue(new retrofit2.Callback<ApiResponse<List<CommentResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CommentResponse>>> call,
                                   Response<ApiResponse<List<CommentResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Comment> comments = CommentMapper.fromResponseList(response.body().getData());
                    // 同步写入本地缓存
                    if (!comments.isEmpty()) {
                        localRepository.insertAll(comments, null);
                    }
                    if (callback != null) {
                        callback.onSuccess(comments);
                    }
                } else {
                    // 网络失败，降级到本地
                    loadFromLocal(postId, callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CommentResponse>>> call, Throwable t) {
                // 网络失败，降级到本地
                loadFromLocal(postId, callback);
            }
        });
    }

    private void loadFromLocal(String postId, Callback<List<Comment>> callback) {
        localRepository.getByPostId(postId, new com.ggg.rememo.core.data.repository.CommentRepository.Callback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * 发布评论。
     */
    public void createComment(String postId, String content, Callback<Comment> callback) {
        CreateCommentRequest request = new CreateCommentRequest(postId, content);
        apiService.createComment(request).enqueue(new retrofit2.Callback<ApiResponse<CommentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CommentResponse>> call,
                                   Response<ApiResponse<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CommentResponse resp = response.body().getData();
                    Comment comment = CommentMapper.fromResponse(resp);
                    // 写入本地
                    if (comment != null) {
                        localRepository.insert(comment, null);
                    }
                    if (callback != null) {
                        callback.onSuccess(comment);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(response.body() != null ? response.body().getMessage() : "发布评论失败");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.onError(NetworkErrorMapper.toUserMessage(t));
                }
            }
        });
    }
}
