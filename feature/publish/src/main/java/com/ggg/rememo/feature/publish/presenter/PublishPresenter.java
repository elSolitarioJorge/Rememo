package com.ggg.rememo.feature.publish.presenter;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.network.response.MemoryPostResponse;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.data.PublishRepository;

import java.util.List;

/**
 * 发布模块 Presenter
 * 职责：参数校验、数据组装、调用 Repository
 */
public class PublishPresenter extends BasePresenter<PublishContract.View>
        implements PublishContract.Presenter {

    private static final String TAG = "PublishPresenter";
    private static final int DEFAULT_YEAR = 2024;
    private static final String DEFAULT_SEASON = "冬";

    private final PublishRepository repository;
    private final Handler mainHandler;

    public PublishPresenter() {
        this.repository = new PublishRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void publish(String title, String content, List<MemoryPhoto> images,
                       double lat, double lng, String pointId, String pointName) {

        if (pointName == null || pointName.trim().isEmpty()) {
            ifViewAttached(view -> view.showError("请为回忆之地命名"));
            return;
        }
        if (pointName.length() > 15) {
            ifViewAttached(view -> view.showError("精简的名称更利于珍藏，请控制在15字以内"));
            return;
        }

        if (title == null || title.trim().isEmpty()) {
            ifViewAttached(view -> view.showError("标题不能为空"));
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            ifViewAttached(view -> view.showError("内容不能为空"));
            return;
        }

        ifViewAttached(view -> {
            String address = view.getAddress();
            String timeDisplayText = view.getTimeDisplayText();

            final int memoryYear;
            final String season;
            if (timeDisplayText != null && !timeDisplayText.isEmpty()
                    && !timeDisplayText.contains("点击选择")) {
                String[] parts = timeDisplayText.split("·");
                if (parts.length >= 2) {
                    memoryYear = Integer.parseInt(parts[0].trim());
                    season = parts[1].trim();
                } else {
                    memoryYear = DEFAULT_YEAR;
                    season = DEFAULT_SEASON;
                }
            } else {
                ifViewAttached(v -> v.showError("请选择记忆发生时间"));
                return;
            }

            // 先并行上传所有图片，获取服务器 photoId 和公网 URL
            repository.uploadPhotosInParallel(images, new PublishRepository.UploadProgressCallback() {
                @Override
                public void onProgress(int completed, int total) {
                    mainHandler.post(() -> ifViewAttached(view1 -> view1.showUploadProgress(completed, total)));
                }
            }, new ApiCallback<List<MemoryPhoto>>() {
                @Override
                public void onSuccess(List<MemoryPhoto> uploadedPhotos) {
                    Log.d(TAG, "所有图片上传成功，开始发布记忆");
                    // 发布记忆到服务器
                    repository.publishMemoryToServer(title, content, uploadedPhotos, memoryYear, season,
                            lat, lng, address, pointId, pointName,
                            new ApiCallback<MemoryPostResponse>() {
                                @Override
                                public void onSuccess(MemoryPostResponse response) {
                                    // 网络发布成功后，同步到本地数据库
                                    repository.saveMemoryFromResponse(response, address,
                                            new com.ggg.rememo.core.data.repository.MemoryPostRepository.Callback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    mainHandler.post(() -> ifViewAttached(PublishContract.View::showPublishSuccess));
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    mainHandler.post(() -> ifViewAttached(view1 -> view1.showError("保存本地失败: " + e.getMessage())));
                                                }
                                            });
                                }

                                @Override
                                public void onError(String message) {
                                    mainHandler.post(() -> ifViewAttached(view1 -> view1.showError("发布失败: " + message)));
                                }
                            });
                }

                @Override
                public void onError(String message) {
                    mainHandler.post(() -> ifViewAttached(view1 -> view1.showError("图片上传失败: " + message)));
                }
            });
        });
    }
}
