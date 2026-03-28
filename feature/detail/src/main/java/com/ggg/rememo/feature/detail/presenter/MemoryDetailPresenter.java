package com.ggg.rememo.feature.detail.presenter;

import android.os.Handler;
import android.os.Looper;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.data.repository.UserRepository;
import com.ggg.rememo.feature.detail.contract.MemoryDetailContract;

public class MemoryDetailPresenter extends BasePresenter<MemoryDetailContract.View>
        implements MemoryDetailContract.Presenter {

    private final MemoryPostRepository postRepository;
    private final UserRepository userRepository;
    private final Handler mainHandler;

    public MemoryDetailPresenter() {
        this.postRepository = new MemoryPostRepository();
        this.userRepository = new UserRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void loadMemory(String postId) {
        if (postId == null || postId.isEmpty()) {
            ifViewAttached(view -> view.showError("记忆不存在"));
            return;
        }

        postRepository.getById(postId, new MemoryPostRepository.Callback<MemoryPost>() {
            @Override
            public void onSuccess(MemoryPost result) {
                if (result == null) {
                    mainHandler.post(() -> {
                        ifViewAttached(view -> view.showError("记忆不存在"));
                    });
                    return;
                }
                // 获取作者名称
                userRepository.getById(result.getAuthorId(), new UserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User userResult) {
                        String authorName = (userResult != null && userResult.getNickname() != null)
                                ? userResult.getNickname()
                                : "匿名用户";
                        mainHandler.post(() -> {
                            ifViewAttached(view -> view.showMemory(result, authorName));
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        mainHandler.post(() -> {
                            ifViewAttached(view -> view.showMemory(result, "匿名用户"));
                        });
                    }
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
        ifViewAttached(view -> view.showToast("❤️ 共鸣信号已发送"));
    }

    @Override
    public void onStarClick() {
        ifViewAttached(view -> view.showToast("⭐ 已写入个人档案"));
    }

    @Override
    public void onImageAiFix(int position) {
        ifViewAttached(view -> {
            view.showToast("SYS: 记忆色彩已恢复");
            view.notifyImageFixed(position);
        });
    }
}
