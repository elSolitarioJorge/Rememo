package com.ggg.rememo.feature.detail.presenter;

import android.os.Handler;
import android.os.Looper;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.data.model.network.response.CollectResponse;
import com.ggg.rememo.core.data.model.network.response.LikeResponse;
import com.ggg.rememo.feature.detail.contract.MemoryDetailContract;
import com.ggg.rememo.feature.detail.data.InteractionRepository;

public class MemoryDetailPresenter extends BasePresenter<MemoryDetailContract.View>
        implements MemoryDetailContract.Presenter {

    private final MemoryPostRepository postRepository;
    private final InteractionRepository interactionRepository;
    private final Handler mainHandler;

    private String currentPostId;

    public MemoryDetailPresenter() {
        this.postRepository = new MemoryPostRepository();
        this.interactionRepository = new InteractionRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadMemory(String postId) {
        if (postId == null || postId.isEmpty()) {
            ifViewAttached(view -> view.showError("记忆不存在"));
            return;
        }
        this.currentPostId = postId;

        postRepository.getById(postId, new MemoryPostRepository.Callback<MemoryPost>() {
            @Override
            public void onSuccess(MemoryPost result) {
                if (result == null) {
                    mainHandler.post(() -> {
                        ifViewAttached(view -> view.showError("记忆不存在"));
                    });
                    return;
                }
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showMemory(result));
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showError("加载失败: " + e.getMessage()));
                });
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
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.updateLikeState(result.isLiked(), result.getLikeCount()));
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showError("点赞失败: " + message));
                });
            }
        });
    }

    @Override
    public void onStarClick() {
        if (currentPostId == null) return;
        interactionRepository.toggleCollect(currentPostId, new InteractionRepository.Callback<CollectResponse>() {
            @Override
            public void onSuccess(CollectResponse result) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.updateCollectState(result.isCollected(), result.getCollectCount()));
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    ifViewAttached(view -> view.showError("收藏失败: " + message));
                });
            }
        });
    }

    @Override
    public void onImageAiFix(int position) {
        ifViewAttached(view -> {
            view.showToast("SYS: 记忆色彩已恢复");
            view.notifyImageFixed(position);
        });
    }
}
