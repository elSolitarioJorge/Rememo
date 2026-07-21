package com.ggg.rememo.feature.publish.data;

/** 可取消的一批图片上传任务。 */
public interface UploadBatchHandle {

    void cancel();

    boolean isCancelled();
}
