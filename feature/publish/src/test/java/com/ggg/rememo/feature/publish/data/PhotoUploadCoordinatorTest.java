package com.ggg.rememo.feature.publish.data;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PhotoUploadCoordinatorTest {

    @Test
    public void emptyList_succeedsOnceWithoutStartingTask() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(Collections.emptyList(), 3, uploader, listener);

        coordinator.start();

        assertEquals(0, uploader.startedIndices.size());
        assertEquals(1, listener.successCount);
        assertEquals(0, listener.errorCount);
        assertTrue(listener.successResult.isEmpty());
    }

    @Test
    public void singlePhoto_reportsOneOfOneAndSucceeds() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(1), 3, uploader, listener);

        coordinator.start();
        uploader.succeed(0);

        assertEquals(Collections.singletonList("1/1"), listener.progressEvents);
        assertEquals(1, listener.successCount);
        assertEquals("photo-0", listener.successResult.get(0).getPhotoId());
    }

    @Test
    public void ninePhotos_neverStartsMoreThanThreeConcurrentTasks() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(9), 3, uploader, listener);

        coordinator.start();
        assertEquals(Arrays.asList(0, 1, 2), uploader.startedIndices);
        assertEquals(3, uploader.maxActive);

        for (int i = 0; i < 9; i++) {
            uploader.succeed(i);
        }

        assertEquals(3, uploader.maxActive);
        assertEquals(1, listener.successCount);
    }

    @Test
    public void outOfOrderCompletion_keepsOriginalPhotoOrder() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(3), 3, uploader, listener);

        coordinator.start();
        uploader.succeed(2);
        uploader.succeed(0);
        uploader.succeed(1);

        assertEquals("photo-0", listener.successResult.get(0).getPhotoId());
        assertEquals("photo-1", listener.successResult.get(1).getPhotoId());
        assertEquals("photo-2", listener.successResult.get(2).getPhotoId());
    }

    @Test
    public void progress_isStrictlyMonotonic() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(4), 2, uploader, listener);

        coordinator.start();
        uploader.succeed(1);
        uploader.succeed(0);
        uploader.succeed(2);
        uploader.succeed(3);

        assertEquals(Arrays.asList("1/4", "2/4", "3/4", "4/4"), listener.progressEvents);
    }

    @Test
    public void oneFailure_waitsForAllAndDeliversOneError() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(4), 2, uploader, listener);

        coordinator.start();
        uploader.fail(1, "timeout");
        assertEquals(0, listener.errorCount);
        uploader.succeed(0);
        uploader.succeed(2);
        uploader.succeed(3);

        assertEquals(0, listener.successCount);
        assertEquals(1, listener.errorCount);
        assertTrue(listener.lastError.contains("1 张图片上传失败"));
        assertTrue(listener.lastError.contains("第 2 张：timeout"));
    }

    @Test
    public void multipleFailures_reportsCountAndFirstIndexedError() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(3), 3, uploader, listener);

        coordinator.start();
        uploader.fail(2, "third failed");
        uploader.fail(0, "first failed");
        uploader.succeed(1);

        assertEquals(1, listener.errorCount);
        assertTrue(listener.lastError.contains("2 张图片上传失败"));
        assertTrue(listener.lastError.contains("第 3 张：third failed"));
    }

    @Test
    public void duplicateTaskCallback_doesNotDeliverTwoTerminalResults() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(1), 1, uploader, listener);

        coordinator.start();
        PhotoUploadCoordinator.PhotoCallback callback = uploader.callbacks.get(0);
        callback.onSuccess(uploader.photos.get(0));
        callback.onError("late error");

        assertEquals(1, listener.successCount);
        assertEquals(0, listener.errorCount);
        assertEquals(1, listener.progressEvents.size());
    }

    @Test
    public void cancel_cancelsActiveTasksAndIgnoresLateCallbacks() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(5), 3, uploader, listener);

        coordinator.start();
        coordinator.cancel();

        assertTrue(coordinator.isCancelled());
        assertEquals(3, uploader.cancelledCount());
        uploader.callbacks.get(0).onSuccess(uploader.photos.get(0));
        uploader.callbacks.get(1).onError("late");
        assertEquals(0, listener.successCount);
        assertEquals(0, listener.errorCount);
        assertTrue(listener.progressEvents.isEmpty());
    }

    @Test
    public void serverUrlsCanCompleteSynchronously_withoutBreakingConcurrencyAccounting() {
        List<MemoryPhoto> photos = createPhotos(5);
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).setOriginalUrl("https://example.com/photo-" + i + ".jpg");
        }
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator.PhotoUploader uploader = (index, photo, callback) -> {
            callback.onSuccess(photo);
            return () -> {
            };
        };
        PhotoUploadCoordinator coordinator = coordinator(photos, 3, uploader, listener);

        coordinator.start();

        assertEquals(1, listener.successCount);
        assertEquals(5, listener.successResult.size());
        assertEquals("photo-4", listener.successResult.get(4).getPhotoId());
    }

    @Test
    public void secondStart_isRejected() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(1), 1, uploader, listener);
        coordinator.start();

        try {
            coordinator.start();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void nullSuccessResult_isTreatedAsFailure() {
        FakeUploader uploader = new FakeUploader();
        RecordingListener listener = new RecordingListener();
        PhotoUploadCoordinator coordinator = coordinator(createPhotos(1), 1, uploader, listener);

        coordinator.start();
        uploader.callbacks.get(0).onSuccess(null);

        assertEquals(0, listener.successCount);
        assertEquals(1, listener.errorCount);
        assertTrue(listener.lastError.contains("上传结果为空"));
    }

    private static PhotoUploadCoordinator coordinator(List<MemoryPhoto> photos,
                                                      int maxConcurrency,
                                                      PhotoUploadCoordinator.PhotoUploader uploader,
                                                      RecordingListener listener) {
        return new PhotoUploadCoordinator(photos, maxConcurrency, uploader, listener);
    }

    private static List<MemoryPhoto> createPhotos(int count) {
        List<MemoryPhoto> photos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MemoryPhoto photo = new MemoryPhoto();
            photo.setPhotoId("photo-" + i);
            photo.setOriginalUrl("/data/photo-" + i + ".jpg");
            photos.add(photo);
        }
        return photos;
    }

    private static final class RecordingListener implements PhotoUploadCoordinator.Listener {
        private final List<String> progressEvents = new ArrayList<>();
        private int successCount;
        private int errorCount;
        private List<MemoryPhoto> successResult;
        private String lastError;

        @Override
        public void onProgress(int completed, int total) {
            progressEvents.add(completed + "/" + total);
        }

        @Override
        public void onSuccess(List<MemoryPhoto> orderedPhotos) {
            successCount++;
            successResult = orderedPhotos;
        }

        @Override
        public void onError(String message) {
            errorCount++;
            lastError = message;
        }
    }

    private static final class FakeUploader implements PhotoUploadCoordinator.PhotoUploader {
        private final List<Integer> startedIndices = new ArrayList<>();
        private final Map<Integer, PhotoUploadCoordinator.PhotoCallback> callbacks = new HashMap<>();
        private final Map<Integer, FakeTask> tasks = new HashMap<>();
        private final Map<Integer, MemoryPhoto> photos = new HashMap<>();
        private int active;
        private int maxActive;

        @Override
        public PhotoUploadCoordinator.CancellableTask upload(
                int index, MemoryPhoto photo, PhotoUploadCoordinator.PhotoCallback callback) {
            startedIndices.add(index);
            callbacks.put(index, callback);
            photos.put(index, photo);
            FakeTask task = new FakeTask();
            tasks.put(index, task);
            active++;
            maxActive = Math.max(maxActive, active);
            return task;
        }

        private void succeed(int index) {
            PhotoUploadCoordinator.PhotoCallback callback = callbacks.get(index);
            assertNotNull("Task " + index + " was not started", callback);
            active--;
            callback.onSuccess(photos.get(index));
        }

        private void fail(int index, String message) {
            PhotoUploadCoordinator.PhotoCallback callback = callbacks.get(index);
            assertNotNull("Task " + index + " was not started", callback);
            active--;
            callback.onError(message);
        }

        private int cancelledCount() {
            int result = 0;
            for (FakeTask task : tasks.values()) {
                if (task.cancelled) {
                    result++;
                }
            }
            return result;
        }

        private static final class FakeTask implements PhotoUploadCoordinator.CancellableTask {
            private boolean cancelled;

            @Override
            public void cancel() {
                cancelled = true;
            }
        }
    }
}
