package com.ggg.rememo.feature.publish.data;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 纯 Java 的受控并发协调器。
 * 只负责调度、保序、进度、错误聚合和取消，不依赖 Android、Retrofit 或 Bitmap。
 */
final class PhotoUploadCoordinator implements UploadBatchHandle {

    interface CancellableTask {
        void cancel();
    }

    interface PhotoCallback {
        void onSuccess(MemoryPhoto photo);

        void onError(String message);
    }

    interface PhotoUploader {
        CancellableTask upload(int index, MemoryPhoto photo, PhotoCallback callback);
    }

    interface Listener {
        void onProgress(int completed, int total);

        void onSuccess(List<MemoryPhoto> orderedPhotos);

        void onError(String message);
    }

    private static final CancellableTask NO_OP_TASK = () -> {
    };

    private final Object lock = new Object();
    private final List<MemoryPhoto> photosSnapshot;
    private final List<MemoryPhoto> results;
    private final int maxConcurrency;
    private final PhotoUploader uploader;
    private final Listener listener;
    private final Map<Integer, CancellableTask> activeTasks = new HashMap<>();
    private final Set<Integer> inFlightIndices = new HashSet<>();

    private int nextIndex;
    private int activeCount;
    private int finishedCount;
    private int failedCount;
    private String firstError;
    private boolean started;
    private boolean cancelled;
    private boolean terminalDelivered;

    PhotoUploadCoordinator(List<MemoryPhoto> photos,
                           int maxConcurrency,
                           PhotoUploader uploader,
                           Listener listener) {
        if (maxConcurrency <= 0) {
            throw new IllegalArgumentException("maxConcurrency must be greater than 0");
        }
        if (uploader == null) {
            throw new IllegalArgumentException("uploader == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener == null");
        }
        this.photosSnapshot = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
        this.results = new ArrayList<>(this.photosSnapshot.size());
        for (int i = 0; i < this.photosSnapshot.size(); i++) {
            this.results.add(null);
        }
        this.maxConcurrency = maxConcurrency;
        this.uploader = uploader;
        this.listener = listener;
    }

    void start() {
        List<Integer> indicesToLaunch;
        boolean emptySuccess;
        synchronized (lock) {
            if (started) {
                throw new IllegalStateException("PhotoUploadCoordinator can only be started once");
            }
            started = true;
            if (cancelled) {
                return;
            }
            emptySuccess = photosSnapshot.isEmpty();
            if (emptySuccess) {
                terminalDelivered = true;
                indicesToLaunch = new ArrayList<>();
            } else {
                indicesToLaunch = scheduleAvailableLocked();
            }
        }

        if (emptySuccess) {
            listener.onSuccess(new ArrayList<>());
            return;
        }
        launchAll(indicesToLaunch);
    }

    @Override
    public void cancel() {
        List<CancellableTask> tasks;
        synchronized (lock) {
            if (cancelled) {
                return;
            }
            cancelled = true;
            terminalDelivered = true;
            tasks = new ArrayList<>(activeTasks.values());
            activeTasks.clear();
            inFlightIndices.clear();
            activeCount = 0;
        }
        for (CancellableTask task : tasks) {
            safelyCancel(task);
        }
    }

    @Override
    public boolean isCancelled() {
        synchronized (lock) {
            return cancelled;
        }
    }

    private void launchAll(List<Integer> indices) {
        for (Integer index : indices) {
            launch(index);
        }
    }

    private void launch(int index) {
        CancellableTask task;
        try {
            task = uploader.upload(index, photosSnapshot.get(index), new PhotoCallback() {
                @Override
                public void onSuccess(MemoryPhoto photo) {
                    if (photo == null) {
                        onPhotoFinished(index, null, "上传结果为空");
                    } else {
                        onPhotoFinished(index, photo, null);
                    }
                }

                @Override
                public void onError(String message) {
                    onPhotoFinished(index, null,
                            message == null || message.trim().isEmpty() ? "未知上传错误" : message);
                }
            });
        } catch (Throwable throwable) {
            onPhotoFinished(index, null,
                    throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage());
            return;
        }

        if (task == null) {
            task = NO_OP_TASK;
        }

        boolean cancelReturnedTask = false;
        synchronized (lock) {
            if (cancelled) {
                cancelReturnedTask = true;
            } else if (inFlightIndices.contains(index)) {
                activeTasks.put(index, task);
            }
        }
        if (cancelReturnedTask) {
            safelyCancel(task);
        }
    }

    private void onPhotoFinished(int index, MemoryPhoto result, String error) {
        int progress;
        int total;
        List<Integer> indicesToLaunch;
        List<MemoryPhoto> successResult = null;
        String finalError = null;

        synchronized (lock) {
            if (cancelled || terminalDelivered || !inFlightIndices.remove(index)) {
                return;
            }
            activeTasks.remove(index);
            activeCount--;
            finishedCount++;
            total = photosSnapshot.size();
            progress = finishedCount;

            if (error == null) {
                results.set(index, result);
            } else {
                failedCount++;
                if (firstError == null) {
                    firstError = "第 " + (index + 1) + " 张：" + error;
                }
            }

            if (finishedCount == total) {
                terminalDelivered = true;
                indicesToLaunch = new ArrayList<>();
                if (failedCount == 0) {
                    successResult = new ArrayList<>(results);
                } else {
                    finalError = failedCount + " 张图片上传失败，首个错误：" + firstError;
                }
            } else {
                indicesToLaunch = scheduleAvailableLocked();
            }
        }

        listener.onProgress(progress, total);
        launchAll(indicesToLaunch);
        if (successResult != null) {
            listener.onSuccess(successResult);
        } else if (finalError != null) {
            listener.onError(finalError);
        }
    }

    private List<Integer> scheduleAvailableLocked() {
        List<Integer> result = new ArrayList<>();
        while (!cancelled
                && !terminalDelivered
                && activeCount < maxConcurrency
                && nextIndex < photosSnapshot.size()) {
            int index = nextIndex++;
            activeCount++;
            inFlightIndices.add(index);
            result.add(index);
        }
        return result;
    }

    private static void safelyCancel(CancellableTask task) {
        if (task == null) {
            return;
        }
        try {
            task.cancel();
        } catch (RuntimeException ignored) {
            // 取消是尽力而为，不能让单个任务异常阻断其余任务取消。
        }
    }
}
