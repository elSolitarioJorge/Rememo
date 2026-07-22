package com.ggg.rememo.feature.publish.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.mapper.MemoryPostMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.request.CreateMemoryPostRequest;
import com.ggg.rememo.core.data.model.network.response.ImageUploadResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostResponse;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.core.data.util.ImageCompressUtil;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 发布模块数据仓库（Model 层）
 */
public class PublishRepository {

    private static final String TAG = "PublishRepository";
    private static final int MAX_CONCURRENT_UPLOADS = 3;
    private static final int PREPROCESS_THREAD_COUNT = 2;

    private final MemoryPointRepository memoryPointRepository;
    private final MemoryPostRepository memoryPostRepository;
    private final ApiService apiService;
    private final ExecutorService imageExecutor;
    private final Object lifecycleLock = new Object();

    @Nullable
    private UploadBatchHandle activeUploadBatch;
    @Nullable
    private Call<ApiResponse<MemoryPostResponse>> activePublishCall;
    private boolean released;

    public PublishRepository() {
        this.memoryPointRepository = new MemoryPointRepository();
        this.memoryPostRepository = new MemoryPostRepository();
        this.apiService = NetworkClient.getInstance().getApiService();
        this.imageExecutor = Executors.newFixedThreadPool(PREPROCESS_THREAD_COUNT);
    }

    /**
     * 图片上传进度回调接口。
     */
    public interface UploadProgressCallback {
        /**
         * 每张图片上传完成时触发。
         * @param completed 已完成的数量
         * @param total 总数量
         */
        void onProgress(int completed, int total);
    }

    /**
     * 以固定窗口上传图片。协调器只管理批次状态，单图任务负责文件和网络生命周期。
     */
    public UploadBatchHandle uploadPhotosControlled(List<MemoryPhoto> photos,
                                                     @Nullable UploadProgressCallback progressCallback,
                                                     ApiCallback<List<MemoryPhoto>> callback) {
        final PhotoUploadCoordinator[] holder = new PhotoUploadCoordinator[1];
        PhotoUploadCoordinator coordinator = new PhotoUploadCoordinator(
                photos,
                MAX_CONCURRENT_UPLOADS,
                (index, photo, photoCallback) -> new SinglePhotoUploadTask(photo, photoCallback).start(),
                new PhotoUploadCoordinator.Listener() {
                    @Override
                    public void onProgress(int completed, int total) {
                        if (progressCallback != null) {
                            progressCallback.onProgress(completed, total);
                        }
                    }

                    @Override
                    public void onSuccess(List<MemoryPhoto> orderedPhotos) {
                        clearActiveBatch(holder[0]);
                        callback.onSuccess(orderedPhotos);
                    }

                    @Override
                    public void onError(String message) {
                        clearActiveBatch(holder[0]);
                        callback.onError(message);
                    }
                });
        holder[0] = coordinator;

        UploadBatchHandle previousBatch;
        boolean rejectBatch;
        synchronized (lifecycleLock) {
            rejectBatch = released;
            if (rejectBatch) {
                previousBatch = null;
            } else {
                previousBatch = activeUploadBatch;
                activeUploadBatch = coordinator;
            }
        }
        if (rejectBatch) {
            coordinator.cancel();
            callback.onError("发布页面已释放");
            return coordinator;
        }
        if (previousBatch != null) {
            previousBatch.cancel();
        }
        coordinator.start();
        return coordinator;
    }

    private void clearActiveBatch(UploadBatchHandle handle) {
        synchronized (lifecycleLock) {
            if (activeUploadBatch == handle) {
                activeUploadBatch = null;
            }
        }
    }

    /**
     * 本地文件必须是应用可见的绝对路径且真实存在；HTTP(S) 地址单独判断。
     */
    private static boolean isLocalFilePath(String value) {
        return hasLocalPathPrefix(value) && new File(value).exists();
    }

    private static boolean hasLocalPathPrefix(String value) {
        return value != null && (value.startsWith("/data/") || value.startsWith("/storage/"));
    }

    private static boolean isServerUrl(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String responseError(Response<ApiResponse<ImageUploadResponse>> response,
                                        String fallback) {
        ApiResponse<ImageUploadResponse> body = response.body();
        if (body != null && !isBlank(body.getMessage())) {
            return body.getMessage();
        }
        return fallback + "（HTTP " + response.code() + "）";
    }

    private static boolean isValidOriginalUpload(Response<ApiResponse<ImageUploadResponse>> response) {
        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
            return false;
        }
        ImageUploadResponse data = response.body().getData();
        return data != null && !isBlank(data.getImageId()) && !isBlank(data.getOriginalUrl());
    }

    private static boolean isValidRestoredUpload(Response<ApiResponse<ImageUploadResponse>> response) {
        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
            return false;
        }
        ImageUploadResponse data = response.body().getData();
        return data != null && !isBlank(data.getOriginalUrl());
    }

    private final class SinglePhotoUploadTask implements PhotoUploadCoordinator.CancellableTask {
        private final MemoryPhoto photo;
        private final PhotoUploadCoordinator.PhotoCallback callback;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean terminalDelivered = new AtomicBoolean();

        @Nullable
        private volatile Future<?> preprocessingFuture;
        @Nullable
        private volatile Call<ApiResponse<ImageUploadResponse>> activeCall;
        @Nullable
        private volatile PreparedUpload activePreparedUpload;

        private SinglePhotoUploadTask(MemoryPhoto photo,
                                      PhotoUploadCoordinator.PhotoCallback callback) {
            this.photo = photo;
            this.callback = callback;
        }

        private SinglePhotoUploadTask start() {
            if (photo == null) {
                finishError("图片数据为空");
                return this;
            }
            submitPreprocessing(this::prepareOriginal);
            return this;
        }

        private void submitPreprocessing(Runnable runnable) {
            if (cancelled.get()) {
                return;
            }
            try {
                preprocessingFuture = imageExecutor.submit(runnable);
            } catch (RejectedExecutionException e) {
                if (!cancelled.get()) {
                    finishError("图片处理线程池已关闭");
                }
            }
        }

        private void prepareOriginal() {
            String originalUrl = photo.getOriginalUrl();
            if (isBlank(originalUrl)) {
                finishError("原图地址为空");
                return;
            }
            if (isServerUrl(originalUrl)) {
                prepareRestored();
                return;
            }
            if (!isLocalFilePath(originalUrl)) {
                if (hasLocalPathPrefix(originalUrl)) {
                    finishError("原图文件不存在: " + originalUrl);
                } else {
                    finishError("不支持的图片地址: " + originalUrl);
                }
                return;
            }

            PreparedUpload prepared;
            try {
                prepared = prepareMultipart(originalUrl);
            } catch (RuntimeException e) {
                finishError("原图预处理失败: " + safeMessage(e));
                return;
            }
            if (cancelled.get()) {
                prepared.cleanup();
                return;
            }
            enqueueOriginal(prepared);
        }

        private PreparedUpload prepareMultipart(String originalPath) {
            String uploadPath = ImageCompressUtil.compressForUpload(originalPath);
            File uploadFile = new File(uploadPath);
            if (!uploadFile.exists() || uploadFile.length() == 0L) {
                throw new IllegalStateException("压缩结果不可用");
            }
            boolean temporary = !uploadFile.getAbsolutePath().equals(new File(originalPath).getAbsolutePath());
            String mimeType = ImageCompressUtil.getMimeType(uploadFile.getAbsolutePath());
            MediaType mediaType = MediaType.parse(mimeType);
            if (mediaType == null) {
                mediaType = MediaType.parse("image/jpeg");
            }
            RequestBody requestBody = RequestBody.create(mediaType, uploadFile);
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file", uploadFile.getName(), requestBody);
            RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "memory");
            return new PreparedUpload(uploadFile, temporary, part, typeBody);
        }

        private void enqueueOriginal(PreparedUpload prepared) {
            activePreparedUpload = prepared;
            Call<ApiResponse<ImageUploadResponse>> call =
                    apiService.uploadImage(prepared.part, prepared.typeBody);
            activeCall = call;
            if (cancelled.get()) {
                call.cancel();
                prepared.cleanup();
                activePreparedUpload = null;
                return;
            }
            call.enqueue(new Callback<ApiResponse<ImageUploadResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                       @NonNull Response<ApiResponse<ImageUploadResponse>> response) {
                    prepared.cleanup();
                    activePreparedUpload = null;
                    activeCall = null;
                    if (cancelled.get()) {
                        return;
                    }
                    if (!isValidOriginalUpload(response)) {
                        finishError(responseError(response, "原图上传响应无效"));
                        return;
                    }
                    ImageUploadResponse data = response.body().getData();
                    photo.setPhotoId(data.getImageId());
                    photo.setOriginalUrl(data.getOriginalUrl());
                    prepareRestored();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                      @NonNull Throwable t) {
                    prepared.cleanup();
                    activePreparedUpload = null;
                    activeCall = null;
                    if (cancelled.get() || call.isCanceled()) {
                        return;
                    }
                    finishError("网络异常: " + safeMessage(t));
                }
            });
        }

        private void prepareRestored() {
            if (cancelled.get()) {
                return;
            }
            String restoredUrl = photo.getRestoredUrl();
            if (isBlank(restoredUrl) || isServerUrl(restoredUrl)) {
                finishSuccess();
                return;
            }
            if (!isLocalFilePath(restoredUrl)) {
                Log.w(TAG, "修复图不可用，降级为原图: " + restoredUrl);
                degradeToOriginal();
                finishSuccess();
                return;
            }
            submitPreprocessing(() -> {
                PreparedUpload prepared;
                try {
                    prepared = prepareMultipart(restoredUrl);
                } catch (RuntimeException e) {
                    Log.w(TAG, "修复图预处理失败，降级为原图", e);
                    degradeToOriginal();
                    finishSuccess();
                    return;
                }
                if (cancelled.get()) {
                    prepared.cleanup();
                    return;
                }
                enqueueRestored(prepared);
            });
        }

        private void enqueueRestored(PreparedUpload prepared) {
            activePreparedUpload = prepared;
            Call<ApiResponse<ImageUploadResponse>> call =
                    apiService.uploadImage(prepared.part, prepared.typeBody);
            activeCall = call;
            if (cancelled.get()) {
                call.cancel();
                prepared.cleanup();
                activePreparedUpload = null;
                return;
            }
            call.enqueue(new Callback<ApiResponse<ImageUploadResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                       @NonNull Response<ApiResponse<ImageUploadResponse>> response) {
                    prepared.cleanup();
                    activePreparedUpload = null;
                    activeCall = null;
                    if (cancelled.get()) {
                        return;
                    }
                    if (isValidRestoredUpload(response)) {
                        photo.setRestoredUrl(response.body().getData().getOriginalUrl());
                    } else {
                        Log.w(TAG, responseError(response, "修复图上传失败，已降级为原图"));
                        degradeToOriginal();
                    }
                    finishSuccess();
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                      @NonNull Throwable t) {
                    prepared.cleanup();
                    activePreparedUpload = null;
                    activeCall = null;
                    if (cancelled.get() || call.isCanceled()) {
                        return;
                    }
                    Log.w(TAG, "修复图上传异常，已降级为原图: " + safeMessage(t));
                    degradeToOriginal();
                    finishSuccess();
                }
            });
        }

        private void degradeToOriginal() {
            photo.setRestoredUrl(null);
            photo.setCurrentState(MemoryPhoto.PhotoState.ORIGINAL);
        }

        private void finishSuccess() {
            if (!cancelled.get() && terminalDelivered.compareAndSet(false, true)) {
                callback.onSuccess(photo);
            }
        }

        private void finishError(String message) {
            if (!cancelled.get() && terminalDelivered.compareAndSet(false, true)) {
                callback.onError(message);
            }
        }

        @Override
        public void cancel() {
            if (!cancelled.compareAndSet(false, true)) {
                return;
            }
            Future<?> future = preprocessingFuture;
            if (future != null) {
                future.cancel(true);
            }
            Call<ApiResponse<ImageUploadResponse>> call = activeCall;
            if (call != null) {
                call.cancel();
            }
            PreparedUpload prepared = activePreparedUpload;
            if (prepared != null) {
                prepared.cleanup();
            }
        }
    }

    private static final class PreparedUpload {
        private final File uploadFile;
        private final boolean temporary;
        private final MultipartBody.Part part;
        private final RequestBody typeBody;
        private final AtomicBoolean cleaned = new AtomicBoolean();

        private PreparedUpload(File uploadFile, boolean temporary,
                               MultipartBody.Part part, RequestBody typeBody) {
            this.uploadFile = uploadFile;
            this.temporary = temporary;
            this.part = part;
            this.typeBody = typeBody;
        }

        private void cleanup() {
            if (!temporary || !cleaned.compareAndSet(false, true)) {
                return;
            }
            if (uploadFile.exists() && !uploadFile.delete()) {
                Log.w(TAG, "临时压缩文件删除失败: " + uploadFile.getAbsolutePath());
            }
        }
    }

    private static String safeMessage(Throwable throwable) {
        return throwable.getMessage() == null
                ? throwable.getClass().getSimpleName()
                : throwable.getMessage();
    }

    /**
     * 发布记忆到服务器。
     * 根据是否有 pointId 决定关联已有记忆点还是新建记忆点。
     *
     * @param title       标题
     * @param content      正文
     * @param images       图片列表
     * @param memoryYear   记忆发生年份
     * @param season       季节
     * @param lat          纬度
     * @param lng          经度
     * @param address      详细地址
     * @param pointId      记忆点ID（可选，为空则新建记忆点）
     * @param pointName    记忆点名称（首次发布时必填）
     * @param callback     发布结果回调
     */
    public Call<ApiResponse<MemoryPostResponse>> publishMemoryToServer(
            String title, String content, List<MemoryPhoto> images,
            int memoryYear, String season, double lat, double lng,
            String address, String pointId, String pointName,
            ApiCallback<MemoryPostResponse> callback) {
        Log.d(TAG, "publishMemoryToServer: 开始发布记忆, pointId=" + pointId + ", pointName=" + pointName);

        List<CreateMemoryPostRequest.MemoryPhotoRequest> imageRequests =
                CreateMemoryPostRequest.MemoryPhotoRequest.fromEntities(images);

        CreateMemoryPostRequest request;
        if (pointId != null && !pointId.isEmpty()) {
            request = new CreateMemoryPostRequest(pointId, null, lat, lng, address,
                    title, content, imageRequests, memoryYear, season);
        } else {
            String nameForPoint = (pointName != null && !pointName.isEmpty()) ? pointName
                    : (address != null ? address : "未命名地点");
            request = new CreateMemoryPostRequest(null, nameForPoint, lat, lng, address,
                    title, content, imageRequests, memoryYear, season);
        }

        Call<ApiResponse<MemoryPostResponse>> publishCall = apiService.createMemoryPost(request);
        Call<ApiResponse<MemoryPostResponse>> previousCall;
        boolean rejectPublish;
        synchronized (lifecycleLock) {
            rejectPublish = released;
            previousCall = rejectPublish ? null : activePublishCall;
            if (!rejectPublish) {
                activePublishCall = publishCall;
            }
        }
        if (rejectPublish) {
            publishCall.cancel();
            callback.onError("发布页面已释放");
            return publishCall;
        }
        if (previousCall != null) {
            previousCall.cancel();
        }
        publishCall.enqueue(new Callback<ApiResponse<MemoryPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MemoryPostResponse>> call,
                                   @NonNull Response<ApiResponse<MemoryPostResponse>> response) {
                clearActivePublishCall(call);
                Log.d(TAG, "publishMemoryToServer 响应 - code=" + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<MemoryPostResponse> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        Log.d(TAG, "publishMemoryToServer 发布成功, postId=" + body.getData().getPostId());
                        callback.onSuccess(body.getData());
                    } else {
                        Log.e(TAG, "publishMemoryToServer 发布失败: " + body.getMessage());
                        callback.onError(body.getMessage());
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "publishMemoryToServer 请求失败: code=" + response.code() + ", errorBody=" + errorBody);
                    callback.onError("发布失败: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<MemoryPostResponse>> call, @NonNull Throwable t) {
                clearActivePublishCall(call);
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "publishMemoryToServer 请求异常", t);
                callback.onFailure(t);
            }
        });
        return publishCall;
    }

    private void clearActivePublishCall(Call<ApiResponse<MemoryPostResponse>> call) {
        synchronized (lifecycleLock) {
            if (activePublishCall == call) {
                activePublishCall = null;
            }
        }
    }

    /**
     * 释放发布页拥有的上传资源。取消是幂等且静默的，不向已销毁 View 回调错误。
     */
    public void release() {
        UploadBatchHandle batch;
        Call<ApiResponse<MemoryPostResponse>> publishCall;
        synchronized (lifecycleLock) {
            if (released) {
                return;
            }
            released = true;
            batch = activeUploadBatch;
            publishCall = activePublishCall;
            activeUploadBatch = null;
            activePublishCall = null;
        }
        if (batch != null) {
            batch.cancel();
        }
        if (publishCall != null) {
            publishCall.cancel();
        }
        imageExecutor.shutdownNow();
    }

    /**
     * 将 API 响应保存到本地数据库。
     * 使用服务器返回的真实 ID 替代本地临时 ID。
     */
    public void saveMemoryFromResponse(MemoryPostResponse response, String address,
                                        MemoryPostRepository.Callback<Boolean> callback) {
        Log.d(TAG, "saveMemoryFromResponse: 开始保存本地, postId=" + response.getPostId());

        MemoryPost memoryPost = MemoryPostMapper.fromPostResponse(response, address);
        if (memoryPost != null
                && (memoryPost.getAuthorId() == null || memoryPost.getAuthorId().trim().isEmpty())) {
            memoryPost.setAuthorId(TokenManager.getUserId());
        }
        memoryPostRepository.insert(memoryPost, new MemoryPostRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d(TAG, "saveMemoryFromResponse 本地保存成功");
                if (callback != null) {
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "saveMemoryFromResponse 本地保存失败", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * 保存记忆（仅本地，已废弃，仅保留供兼容）
     * @deprecated 请使用 {@link #publishMemoryToServer} 先上传服务器
     */
    @Deprecated
    public void saveMemory(String title, String content, List<MemoryPhoto> images,
                          int memoryYear, String season, double lat, double lng, String address,
                          MemoryPostRepository.Callback<Boolean> callback) {
        saveMemoryWithPointId(title, content, images, memoryYear, season, lat, lng, address, null, callback);
    }

    /**
     * 保存记忆（可选指定记忆点，仅本地，已废弃，仅保留供兼容）
     * @deprecated 请使用 {@link #publishMemoryToServer} 先上传服务器
     */
    @Deprecated
    public void saveMemoryWithPointId(String title, String content, List<MemoryPhoto> images,
                          int memoryYear, String season, double lat, double lng, String address,
                          String pointId, MemoryPostRepository.Callback<Boolean> callback) {

        final String postId = UUID.randomUUID().toString();

        if (pointId != null && !pointId.isEmpty()) {
            createMemoryPost(title, content, images, memoryYear, season, pointId, postId, callback);
        } else {
            final String newPointId = UUID.randomUUID().toString();
            MemoryPoint memoryPoint = new MemoryPoint();
            memoryPoint.setPointId(newPointId);
            memoryPoint.setLatitude(lat);
            memoryPoint.setLongitude(lng);
            memoryPoint.setPointName(address != null ? address : "未命名地点");
            memoryPoint.setLocationAddress(address);
            memoryPoint.setCoverImageUrl(images != null && !images.isEmpty() ?
                images.get(0).getDisplayUrl() : null);
            memoryPoint.setMemoryCount(1);
            memoryPoint.setMinYear(memoryYear);
            memoryPoint.setMaxYear(memoryYear);
            memoryPoint.setCreatedTime(System.currentTimeMillis());

            memoryPointRepository.insert(memoryPoint, new MemoryPointRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    createMemoryPost(title, content, images, memoryYear, season, newPointId, postId, callback);
                }

                @Override
                public void onError(Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }
            });
        }
    }

    /**
     * 创建记忆并保存
     * @deprecated 请使用 {@link #publishMemoryToServer} 先上传服务器
     */
    @Deprecated
    private void createMemoryPost(String title, String content, List<MemoryPhoto> images,
                                  int memoryYear, String season, String pointId, String postId,
                                  MemoryPostRepository.Callback<Boolean> callback) {
        String authorId = TokenManager.getUserId();
        if (authorId == null || authorId.isEmpty()) {
            authorId = "default_user";
        }

        MemoryPost memoryPost = new MemoryPost();
        memoryPost.setPostId(postId);
        memoryPost.setPointId(pointId);
        memoryPost.setAuthorId(authorId);
        memoryPost.setTitle(title);
        memoryPost.setContent(content);
        memoryPost.setMemoryYear(memoryYear);
        memoryPost.setMemorySeason(season);

        if (images != null && !images.isEmpty()) {
            memoryPost.setImages(new ArrayList<>(images));
        }

        memoryPost.setLikeCount(0);
        memoryPost.setCommentCount(0);
        memoryPost.setCollectCount(0);
        memoryPost.setCreatedTime(System.currentTimeMillis());

        memoryPostRepository.insert(memoryPost, new MemoryPostRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}
