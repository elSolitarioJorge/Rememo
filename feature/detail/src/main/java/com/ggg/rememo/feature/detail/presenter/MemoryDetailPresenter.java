package com.ggg.rememo.feature.detail.presenter;

import android.os.Handler;
import android.os.Looper;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.data.model.network.response.CollectResponse;
import com.ggg.rememo.core.data.model.network.response.LikeResponse;
import com.ggg.rememo.feature.detail.contract.MemoryDetailContract;
import com.ggg.rememo.feature.detail.data.CommentRepository;
import com.ggg.rememo.feature.detail.data.InteractionRepository;
import com.ggg.rememo.feature.detail.data.MemoryDetailRepository;

import java.util.List;

public class MemoryDetailPresenter extends BasePresenter<MemoryDetailContract.View>
        implements MemoryDetailContract.Presenter {

    private final MemoryDetailRepository memoryDetailRepository;
    private final InteractionRepository interactionRepository;
    private final CommentRepository commentRepository;
    private final MemoryPointRepository pointRepository;
    private final Handler mainHandler;

    private String currentPostId;

    public MemoryDetailPresenter() {
        this.memoryDetailRepository = new MemoryDetailRepository();
        this.interactionRepository = new InteractionRepository();
        this.commentRepository = new CommentRepository();
        this.pointRepository = new MemoryPointRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadMemory(String postId) {
        if (postId == null || postId.isEmpty()) {
            ifViewAttached(view -> view.showError("记忆不存在"));
            return;
        }
        this.currentPostId = postId;

        memoryDetailRepository.loadMemory(postId, new MemoryDetailRepository.Callback<MemoryPost>() {
            @Override
            public void onSuccess(MemoryPost result) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showMemory(result));

                    String pointId = result.getPointId();
                    if (pointId != null && !pointId.isEmpty()) {
                        loadMemoryPoint(pointId);
                    }
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> ifViewAttached(view -> view.showError("加载失败: " + message)));
            }
        });
    }

    @Override
    public void onShareClick() {
        ifViewAttached(view -> view.showToast("启动全息明信片生成引擎..."));
    }

    @Override
    public void onLikeClick() {
        if (currentPostId == null) return;
        interactionRepository.toggleLike(currentPostId, new InteractionRepository.Callback<LikeResponse>() {
            @Override
            public void onSuccess(LikeResponse result) {
                mainHandler.post(() -> ifViewAttached(view -> view.updateLikeState(result.isLiked(), result.getLikeCount())));
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> ifViewAttached(view -> view.showError("点赞失败: " + message)));
            }
        });
    }

    @Override
    public void onStarClick() {
        if (currentPostId == null) return;
        interactionRepository.toggleCollect(currentPostId, new InteractionRepository.Callback<CollectResponse>() {
            @Override
            public void onSuccess(CollectResponse result) {
                mainHandler.post(() -> ifViewAttached(view -> view.updateCollectState(result.isCollected(), result.getCollectCount())));
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> ifViewAttached(view -> view.showError("收藏失败: " + message)));
            }
        });
    }

    @Override
    public void onImageAiFix(int position) {
        ifViewAttached(view -> view.notifyImageFixed(position));
    }

    @Override
    public void loadComments(String postId) {
        if (postId == null || postId.isEmpty()) return;

        commentRepository.loadComments(postId, 1, 20, new CommentRepository.Callback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                mainHandler.post(() -> ifViewAttached(view -> view.showComments(result)));
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> ifViewAttached(view -> view.showError("加载评论失败: " + message)));
            }
        });
    }

    private void loadMemoryPoint(String pointId) {
        pointRepository.getById(pointId, new MemoryPointRepository.Callback<MemoryPoint>() {
            @Override
            public void onSuccess(MemoryPoint result) {
                mainHandler.post(() -> ifViewAttached(view -> view.showMemoryPoint(result)));
            }

            @Override
            public void onError(Exception e) {
                // 记忆点加载失败不影响主流程，静默处理
            }
        });
    }

    @Override
    public void onSendComment(String postId, String content) {
        if (postId == null || postId.isEmpty() || content == null || content.trim().isEmpty()) {
            return;
        }

        commentRepository.createComment(postId, content.trim(), new CommentRepository.Callback<Comment>() {
            @Override
            public void onSuccess(Comment result) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> {
                        view.showCommentSendSuccess();
                        view.showToast("评论发布成功");
                    });
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> ifViewAttached(view -> view.showError("发送评论失败: " + message)));
            }
        });
    }
}
