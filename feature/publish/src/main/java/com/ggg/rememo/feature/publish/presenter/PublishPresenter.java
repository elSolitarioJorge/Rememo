package com.ggg.rememo.feature.publish.presenter;

import android.os.Handler;
import android.os.Looper;

import android.os.SystemClock;
import android.util.Log;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.network.response.MemoryPostResponse;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.data.PublishRepository;
import com.ggg.rememo.feature.publish.data.UploadBatchHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;

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
    private boolean isPublishing;
    private UploadBatchHandle activeUploadBatch;
    private Call<ApiResponse<MemoryPostResponse>> activePublishCall;

    public PublishPresenter() {
        this.repository = new PublishRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void publish(String title, String content, List<MemoryPhoto> images,
                       double lat, double lng, String pointId, String pointName) {
        if (isPublishing) {
            Log.d(TAG, "忽略重复发布点击");
            return;
        }
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

        PublishContract.View view = getView();
        if (view == null) {
            return;
        }
        String address = view.getAddress();
        ParsedTime parsedTime = parseTime(view.getTimeDisplayText());
        if (parsedTime == null) {
            view.showError("请选择有效的记忆发生时间");
            return;
        }

        List<MemoryPhoto> photosSnapshot = createPhotoSnapshot(images);
        isPublishing = true;
        view.setPublishingState(true);
        if (!photosSnapshot.isEmpty()) {
            view.showUploadProgress(0, photosSnapshot.size());
        }

        AtomicBoolean uploadTerminal = new AtomicBoolean();
        UploadBatchHandle handle = repository.uploadPhotosControlled(
                photosSnapshot,
                (completed, total) -> mainHandler.post(() -> {
                    if (isPublishing) {
                        ifViewAttached(v -> v.showUploadProgress(completed, total));
                    }
                }),
                new ApiCallback<List<MemoryPhoto>>() {
                    @Override
                    public void onSuccess(List<MemoryPhoto> uploadedPhotos) {
                        uploadTerminal.set(true);
                        mainHandler.post(() -> {
                            if (!isPublishing) {
                                return;
                            }
                            activeUploadBatch = null;
                            publishPost(title, content, uploadedPhotos,
                                    parsedTime.year, parsedTime.season,
                                    lat, lng, address, pointId, pointName);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        uploadTerminal.set(true);
                        mainHandler.post(() -> finishWithError("图片上传失败: " + message));
                    }
                });
        if (!uploadTerminal.get() && isPublishing) {
            activeUploadBatch = handle;
        }
    }

    private void publishPost(String title, String content, List<MemoryPhoto> uploadedPhotos,
                             int memoryYear, String season, double lat, double lng,
                             String address, String pointId, String pointName) {
        activePublishCall = repository.publishMemoryToServer(
                title, content, uploadedPhotos, memoryYear, season,
                lat, lng, address, pointId, pointName,
                new ApiCallback<MemoryPostResponse>() {
                    @Override
                    public void onSuccess(MemoryPostResponse response) {
                        mainHandler.post(() -> activePublishCall = null);
                        repository.saveMemoryFromResponse(response, address,
                                new MemoryPostRepository.Callback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        mainHandler.post(PublishPresenter.this::finishWithSuccess);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "帖子已发布，但本地缓存写入失败，等待网络刷新同步", e);
                                        mainHandler.post(PublishPresenter.this::finishWithSuccess);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        mainHandler.post(() -> {
                            activePublishCall = null;
                            finishWithError("发布失败: " + message);
                        });
                    }
                });
    }

    private ParsedTime parseTime(String displayText) {
        if (displayText == null || displayText.trim().isEmpty()
                || displayText.contains("点击选择")) {
            return null;
        }
        String[] parts = displayText.split("·");
        if (parts.length < 2) {
            return new ParsedTime(DEFAULT_YEAR, DEFAULT_SEASON);
        }
        try {
            return new ParsedTime(Integer.parseInt(parts[0].trim()), parts[1].trim());
        } catch (NumberFormatException e) {
            Log.w(TAG, "记忆时间解析失败: " + displayText, e);
            return null;
        }
    }

    private List<MemoryPhoto> createPhotoSnapshot(List<MemoryPhoto> images) {
        List<MemoryPhoto> snapshot = new ArrayList<>();
        if (images == null) {
            return snapshot;
        }
        for (MemoryPhoto source : images) {
            if (source == null) {
                snapshot.add(null);
                continue;
            }
            snapshot.add(new MemoryPhoto(
                    source.getPhotoId(),
                    source.getOriginalUrl(),
                    source.getRestoredUrl(),
                    source.getCurrentState()));
        }
        return snapshot;
    }

    private void finishWithError(String message) {
        if (!isPublishing) {
            return;
        }
        isPublishing = false;
        activeUploadBatch = null;
        activePublishCall = null;
        ifViewAttached(view -> {
            view.setPublishingState(false);
            view.showError(message);
        });
    }

    private void finishWithSuccess() {
        if (!isPublishing) {
            return;
        }
        isPublishing = false;
        activeUploadBatch = null;
        activePublishCall = null;
        ifViewAttached(view -> {
            view.setPublishingState(false);
            view.showPublishSuccess();
        });
    }

    @Override
    protected void onViewDetached() {
        isPublishing = false;
        if (activeUploadBatch != null) {
            activeUploadBatch.cancel();
            activeUploadBatch = null;
        }
        if (activePublishCall != null) {
            activePublishCall.cancel();
            activePublishCall = null;
        }
        repository.release();
        mainHandler.removeCallbacksAndMessages(null);
        super.onViewDetached();
    }

    private static final class ParsedTime {
        private final int year;
        private final String season;

        private ParsedTime(int year, String season) {
            this.year = year;
            this.season = season;
        }
    }
}
