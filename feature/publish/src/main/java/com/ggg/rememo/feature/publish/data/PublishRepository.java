package com.ggg.rememo.feature.publish.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final MemoryPointRepository memoryPointRepository;
    private final MemoryPostRepository memoryPostRepository;
    private final ApiService apiService;

    public PublishRepository() {
        this.memoryPointRepository = new MemoryPointRepository();
        this.memoryPostRepository = new MemoryPostRepository();
        this.apiService = NetworkClient.getInstance().getApiService();
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
     * 上传单张图片到服务器，并回填 photoId 和服务器 URL。
     * 如果图片已经是服务器 URL（不是本地路径），则跳过上传。
     * 上传前自动压缩本地图片，并根据真实文件类型设置 MIME。
     *
     * @param photo 要上传的照片
     * @param callback 上传结果回调，回调中的 photo 已回填 photoId + 服务器 URL
     */
    public void uploadPhoto(MemoryPhoto photo, ApiCallback<MemoryPhoto> callback) {
        uploadPhotoWithCompress(photo, callback);
    }

    /**
     * 上传单张图片（支持压缩版），内部使用。
     */
    private void uploadPhotoWithCompress(MemoryPhoto photo, ApiCallback<MemoryPhoto> callback) {
        Log.d(TAG, "uploadPhoto: photoId=" + photo.getPhotoId() + ", originalUrl=" + photo.getOriginalUrl());

        // 如果 originalUrl 不是本地路径（已经是服务器 URL），跳过上传
        if (photo.getOriginalUrl() != null && !photo.getOriginalUrl().startsWith("/data/")
                && !photo.getOriginalUrl().startsWith("/storage/")) {
            Log.d(TAG, "uploadPhoto: originalUrl 已是服务器地址，跳过上传");
            callback.onSuccess(photo);
            return;
        }

        File originalFile = new File(photo.getOriginalUrl());
        if (!originalFile.exists()) {
            callback.onError("图片文件不存在: " + photo.getOriginalUrl());
            return;
        }

        // 压缩图片
        String compressedPath = ImageCompressUtil.compressForUpload(photo.getOriginalUrl());
        File uploadFile = new File(compressedPath);
        boolean isCompressed = !compressedPath.equals(photo.getOriginalUrl());

        // 动态获取 MIME 类型
        String mimeType = ImageCompressUtil.getMimeType(uploadFile.getAbsolutePath());
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), uploadFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", uploadFile.getName(), requestBody);
        RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "memory");

        apiService.uploadImage(part, typeBody).enqueue(new Callback<ApiResponse<ImageUploadResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                   @NonNull Response<ApiResponse<ImageUploadResponse>> response) {
                // 清理压缩临时文件
                if (isCompressed) {
                    cleanupCompressedFile(compressedPath);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ImageUploadResponse uploadResp = response.body().getData();
                    photo.setPhotoId(uploadResp.getImageId());
                    photo.setOriginalUrl(uploadResp.getOriginalUrl());
                    Log.d(TAG, "uploadPhoto 原始图上传成功: imageId=" + uploadResp.getImageId() + ", url=" + uploadResp.getOriginalUrl());

                    uploadRestoredIfNeeded(photo, callback);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "上传失败: " + response.code();
                    Log.e(TAG, "uploadPhoto 原始图上传失败: " + msg);
                    callback.onError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ImageUploadResponse>> call, @NonNull Throwable t) {
                if (isCompressed) {
                    cleanupCompressedFile(compressedPath);
                }
                Log.e(TAG, "uploadPhoto 原始图上传异常", t);
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 如果修复图是本地路径，上传修复图到服务器并回填 restoredUrl。
     * 注意：修复图上传失败不影响整体流程，原图已上传成功即可。
     */
    private void uploadRestoredIfNeeded(MemoryPhoto photo, ApiCallback<MemoryPhoto> callback) {
        String restoredUrl = photo.getRestoredUrl();
        if (restoredUrl == null || restoredUrl.isEmpty()
                || (!restoredUrl.startsWith("/data/") && !restoredUrl.startsWith("/storage/"))) {
            callback.onSuccess(photo);
            return;
        }

        File restoredFile = new File(restoredUrl);
        if (!restoredFile.exists()) {
            callback.onSuccess(photo);
            return;
        }

        String compressedPath = ImageCompressUtil.compressForUpload(restoredUrl);
        File uploadFile = new File(compressedPath);
        boolean isCompressed = !compressedPath.equals(restoredUrl);

        String mimeType = ImageCompressUtil.getMimeType(uploadFile.getAbsolutePath());
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), uploadFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", uploadFile.getName(), requestBody);
        RequestBody typeBody = RequestBody.create(MediaType.parse("text/plain"), "memory");

        apiService.uploadImage(part, typeBody).enqueue(new Callback<ApiResponse<ImageUploadResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ImageUploadResponse>> call,
                                   @NonNull Response<ApiResponse<ImageUploadResponse>> response) {
                if (isCompressed) {
                    cleanupCompressedFile(compressedPath);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ImageUploadResponse uploadResp = response.body().getData();
                    photo.setRestoredUrl(uploadResp.getOriginalUrl());
                    Log.d(TAG, "uploadRestoredIfNeeded 修复图上传成功: url=" + uploadResp.getOriginalUrl());
                    callback.onSuccess(photo);
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "修复图上传失败: " + response.code();
                    Log.e(TAG, "uploadRestoredIfNeeded 修复图上传失败（不阻断）: " + msg);
                    callback.onSuccess(photo);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ImageUploadResponse>> call, @NonNull Throwable t) {
                if (isCompressed) {
                    cleanupCompressedFile(compressedPath);
                }
                Log.e(TAG, "uploadRestoredIfNeeded 修复图上传异常（不阻断）", t);
                callback.onSuccess(photo);
            }
        });
    }

    /**
     * 上传多张图片。
     * 按顺序逐张上传，上传成功后再调用回调。
     * 如果任何一张上传失败，立即触发错误回调。
     *
     * @param photos 图片列表
     * @param callback 全部上传成功后的回调，data 为回填后的照片列表
     */
    public void uploadPhotos(List<MemoryPhoto> photos, ApiCallback<List<MemoryPhoto>> callback) {
        if (photos == null || photos.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        final List<MemoryPhoto> uploadedPhotos = new ArrayList<>();
        final int[] index = {0};

        uploadNext(photos, uploadedPhotos, index, callback);
    }

    private void uploadNext(List<MemoryPhoto> photos, List<MemoryPhoto> uploadedPhotos,
                            int[] index, ApiCallback<List<MemoryPhoto>> callback) {
        if (index[0] >= photos.size()) {
            callback.onSuccess(uploadedPhotos);
            return;
        }

        MemoryPhoto photo = photos.get(index[0]);
        uploadPhoto(photo, new ApiCallback<MemoryPhoto>() {
            @Override
            public void onSuccess(MemoryPhoto uploadedPhoto) {
                uploadedPhotos.add(uploadedPhoto);
                index[0]++;
                uploadNext(photos, uploadedPhotos, index, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError("第 " + (index[0] + 1) + " 张图片上传失败: " + message);
            }
        });
    }

    /**
     * 并行上传多张图片。
     * 所有图片同时上传，每张完成后通过进度回调通知上层。
     * 只有全部图片都成功时才返回成功列表；任意一张失败则触发错误回调。
     *
     * @param photos           图片列表
     * @param progressCallback 进度回调（可为 null）
     * @param callback         上传结果回调
     */
    public void uploadPhotosInParallel(List<MemoryPhoto> photos,
                                        UploadProgressCallback progressCallback,
                                        ApiCallback<List<MemoryPhoto>> callback) {
        if (photos == null || photos.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        final List<MemoryPhoto> uploadedPhotos = Collections.synchronizedList(new ArrayList<>());
        final int total = photos.size();
        final CountDownLatch latch = new CountDownLatch(total);
        final AtomicInteger completed = new AtomicInteger(0);
        final AtomicInteger failedCount = new AtomicInteger(0);
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        for (MemoryPhoto photo : photos) {
            uploadPhotoWithCompress(photo, new ApiCallback<MemoryPhoto>() {
                @Override
                public void onSuccess(MemoryPhoto result) {
                    uploadedPhotos.add(result);
                    int done = completed.incrementAndGet();
                    if (progressCallback != null) {
                        int progress = done;
                        mainHandler.post(() -> progressCallback.onProgress(progress, total));
                    }
                    latch.countDown();
                }

                @Override
                public void onError(String message) {
                    failedCount.incrementAndGet();
                    int done = completed.incrementAndGet();
                    if (progressCallback != null) {
                        int progress = done;
                        mainHandler.post(() -> progressCallback.onProgress(progress, total));
                    }
                    latch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await();
                mainHandler.post(() -> {
                    if (failedCount.get() > 0) {
                        callback.onError(failedCount.get() + " 张图片上传失败");
                    } else {
                        callback.onSuccess(new ArrayList<>(uploadedPhotos));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                mainHandler.post(() -> callback.onError("上传被中断"));
            }
        }).start();
    }

    /**
     * 清理上传过程中产生的压缩临时文件。
     */
    private void cleanupCompressedFile(String path) {
        if (path != null && path.contains("_compressed_")) {
            File f = new File(path);
            if (f.exists()) {
                if (f.delete()) {
                    Log.d(TAG, "cleanupCompressedFile: 已删除临时文件 " + path);
                } else {
                    Log.w(TAG, "cleanupCompressedFile: 删除临时文件失败 " + path);
                }
            }
        }
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
    public void publishMemoryToServer(String title, String content, List<MemoryPhoto> images,
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

        apiService.createMemoryPost(request).enqueue(new Callback<ApiResponse<MemoryPostResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MemoryPostResponse>> call,
                                   @NonNull Response<ApiResponse<MemoryPostResponse>> response) {
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
                Log.e(TAG, "publishMemoryToServer 请求异常", t);
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 将 API 响应保存到本地数据库。
     * 使用服务器返回的真实 ID 替代本地临时 ID。
     */
    public void saveMemoryFromResponse(MemoryPostResponse response, String address,
                                        MemoryPostRepository.Callback<Boolean> callback) {
        Log.d(TAG, "saveMemoryFromResponse: 开始保存本地, postId=" + response.getPostId());

        MemoryPost memoryPost = MemoryPostMapper.fromPostResponse(response, address);
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
